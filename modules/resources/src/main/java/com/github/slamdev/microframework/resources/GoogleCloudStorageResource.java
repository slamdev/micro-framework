package com.github.slamdev.microframework.resources;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import lombok.Value;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.Optional;

@Value
public class GoogleCloudStorageResource implements Resource {

    String bucketName;
    String objectName;
    Storage storage;

    @SuppressWarnings("Guava")
    Supplier<Optional<Blob>> objectMetadata = Suppliers.memoize(this::getObjectMetadata);

    @Override
    public InputStream getInputStream() {
        BlobId id = BlobId.of(bucketName, objectName);
        return Channels.newInputStream(storage.reader(id));
    }

    @Override
    public String getName() {
        return objectName;
    }

    @Override
    public boolean delete() {
        return storage.delete(BlobId.of(bucketName, objectName));
    }

    @Override
    public String getPath() {
        return getObjectMetadata().orElseThrow(IllegalArgumentException::new).getSelfLink();
    }

    @Override
    public boolean exists() {
        return objectMetadata.get().isPresent();
    }

    private Optional<Blob> getObjectMetadata() {
        try {
            BlobId id = BlobId.of(bucketName, objectName);
            return Optional.of(storage.get(id));
        } catch (StorageException e) {
            // Catch 404 (object not found) and 301 (bucket not found, moved permanently)
            if (e.getCode() == 404 || e.getCode() == 301) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }
}
