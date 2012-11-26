package com.carrotgarden.nexus.example.scanner;

import javax.inject.Singleton;

import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.plugin.Managed;

@Managed
@Singleton
public interface Scanner
{
    boolean hasVirus( StorageFileItem file );
}
