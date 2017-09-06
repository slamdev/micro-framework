package com.github.slamdev.microframework.resources;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.github.slamdev.microframework.resources.AntPathMatcher.isPattern;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Value
public class GoogleCloudStorageResourceResolver implements ResourceResolver<GoogleCloudStorageResource> {

    private static final String PROTOCOL = "gcs://";

    private static final String PATH_DELIMITER = "/";

    private static final String VERSION_DELIMITER = "^";

    private static final AntPathMatcher MATCHER = new AntPathMatcher.Builder().build();

    Storage storage;

    @Override
    public GoogleCloudStorageResource getResource(String location) {
        return new GoogleCloudStorageResource(getBucketName(location), getObjectName(location), storage);
    }

    @Override
    public List<GoogleCloudStorageResource> getResources(String pattern) {
        if (isPattern(pattern)) {
            return getResourcesFromPattern(pattern);
        }
        return singletonList(getResource(pattern));
    }

    private List<GoogleCloudStorageResource> getResourcesFromPattern(String pattern) {
        String bucketPattern = getBucketName(pattern);
        String keyPattern = getObjectName(pattern);
        Set<GoogleCloudStorageResource> resources;
        if (isPattern(bucketPattern)) {
            List<String> matchingBuckets = findMatchingBuckets(bucketPattern);
            // If the '**' wildcard is used in the bucket name, one have to inspect all
            // objects in the bucket. Therefore the keyPattern is prefixed with '**/' so
            // that the findPathMatchingKeys method knows that it must go through all objects.
            if (bucketPattern.startsWith("**")) {
                keyPattern = "**/" + keyPattern;
            }
            return findPathMatchingKeys(keyPattern, matchingBuckets);
        }
        return findPathMatchingKeys(keyPattern, singletonList(bucketPattern));
    }

    private List<GoogleCloudStorageResource> findPathMatchingKeys(String keyPattern, List<String> matchingBuckets) {
        List<GoogleCloudStorageResource> resources = new ArrayList<>();
        if (isPattern(keyPattern)) {
            for (String bucketName : matchingBuckets) {
                findPathMatchingKeyInBucket(bucketName, resources, null, keyPattern);
            }
        } else {
            for (String matchingBucket : matchingBuckets) {
                GoogleCloudStorageResource resource = getResource(getLocationForBucketAndObject(matchingBucket, keyPattern));
                if (resource.exists()) {
                    resources.add(resource);
                }
            }
        }
        return resources;
    }

    private void findPathMatchingKeyInBucket(String bucketName, List<GoogleCloudStorageResource> resources, String prefix, String keyPattern) {
        String remainingPatternPart = getRemainingPatternPart(keyPattern, prefix);
        if (remainingPatternPart != null && remainingPatternPart.startsWith("**")) {
            findAllResourcesThatMatches(bucketName, resources, prefix, keyPattern);
        } else {
            findProgressivelyWithPartialMatch(bucketName, resources, prefix, keyPattern);
        }
    }

    private void findAllResourcesThatMatches(String bucketName, List<GoogleCloudStorageResource> resources, String prefix, String keyPattern) {
        Page<Blob> page = prefix == null ? storage.list(bucketName) : storage.list(bucketName, Storage.BlobListOption.prefix(prefix));
        List<Blob> blobs = new ArrayList<>();
        page.iterateAll().forEach(blobs::add);
        resources.addAll(getResourcesFromObjectSummaries(bucketName, keyPattern, blobs));
    }

    /**
     * Searches for matching keys progressively. This means that instead of retrieving all keys given a prefix, it goes
     * down one level at a time and filters out all non-matching results. This avoids a lot of unused requests results.
     * WARNING: This method does not truncate results. Therefore all matching resources will be returned regardless of
     * the truncation.
     */
    private void findProgressivelyWithPartialMatch(String bucketName, List<GoogleCloudStorageResource> resources, String prefix, String keyPattern) {
        findAllResourcesThatMatches(bucketName, resources, prefix, keyPattern);
    }

    private String getRemainingPatternPart(String keyPattern, String path) {
        int numberOfSlashes = countOccurrencesOf(path, "/");
        int indexOfNthSlash = getIndexOfNthOccurrence(keyPattern, "/", numberOfSlashes);
        return indexOfNthSlash == -1 ? null : keyPattern.substring(indexOfNthSlash);
    }

    private static int countOccurrencesOf(String str, String sub) {
        if (str == null || str.isEmpty() || sub == null || sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    private int getIndexOfNthOccurrence(String str, String sub, int pos) {
        int result = 0;
        String subStr = str;
        for (int i = 0; i < pos; i++) {
            int nthOccurrence = subStr.indexOf(sub);
            if (nthOccurrence == -1) {
                return -1;
            } else {
                result += nthOccurrence + 1;
                subStr = subStr.substring(nthOccurrence + 1);
            }
        }
        return result;
    }

    private List<GoogleCloudStorageResource> getResourcesFromObjectSummaries(String bucketName, String keyPattern, List<Blob> objectSummaries) {
        return objectSummaries.stream()
                .filter(b -> MATCHER.isMatch(keyPattern, b.getName()))
                .map(b -> getLocationForBucketAndObject(bucketName, b.getName()))
                .map(this::getResource)
                .filter(Resource::exists)
                .collect(toList());
    }

    private List<String> findMatchingBuckets(String bucketPattern) {
        List<Bucket> buckets = new ArrayList<>();
        Page<Bucket> page = storage.list();
        page.iterateAll().forEach(buckets::add);
        return buckets.stream()
                .map(Bucket::getName)
                .filter(n -> MATCHER.isMatch(bucketPattern, n))
                .collect(toList());
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    private static String getBucketName(String location) {
        int bucketEndIndex = location.indexOf(PATH_DELIMITER, PROTOCOL.length());
        if (bucketEndIndex == -1 || bucketEndIndex == PROTOCOL.length()) {
            throw new IllegalArgumentException("The location :'" + location + "' does not contain a valid bucket name");
        }
        return location.substring(PROTOCOL.length(), bucketEndIndex);
    }

    private static String getObjectName(String location) {
        int bucketEndIndex = location.indexOf(PATH_DELIMITER, PROTOCOL.length());
        if (bucketEndIndex == -1 || bucketEndIndex == PROTOCOL.length()) {
            throw new IllegalArgumentException("The location :'" + location + "' does not contain a valid bucket name");
        }
        if (location.contains(VERSION_DELIMITER)) {
            return getObjectName(location.substring(0, location.indexOf(VERSION_DELIMITER)));
        }
        if (location.endsWith(PATH_DELIMITER)) {
            return location.substring(++bucketEndIndex, location.length() - 1);
        }
        return location.substring(++bucketEndIndex, location.length());
    }

    private static String stripProtocol(String location) {
        return location.substring(PROTOCOL.length());
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private static String getLocationForBucketAndObject(String bucketName, String objectName) {
        StringBuilder location = new StringBuilder(PROTOCOL.length()
                + bucketName.length()
                + PATH_DELIMITER.length()
                + objectName.length());
        location.append(PROTOCOL);
        location.append(bucketName);
        location.append(PATH_DELIMITER);
        location.append(objectName);
        return location.toString();
    }
}
