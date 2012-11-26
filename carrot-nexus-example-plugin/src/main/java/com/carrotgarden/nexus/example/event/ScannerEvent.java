package com.carrotgarden.nexus.example.event;

import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.AbstractEvent;

public class ScannerEvent
    extends AbstractEvent<Repository>
{
    private final StorageFileItem file;

    public ScannerEvent( Repository component, StorageFileItem file )
    {
        super( component );

        this.file = file;
    }

    public StorageFileItem getInfectedFile()
    {
        return file;
    }
}
