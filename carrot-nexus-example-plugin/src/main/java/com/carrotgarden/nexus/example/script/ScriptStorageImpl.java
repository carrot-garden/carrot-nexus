/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.carrotgarden.nexus.example.script;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.plexus.appevents.Event;

@Component(role = ScriptStorage.class)
public class ScriptStorageImpl implements ScriptStorage, Initializable,
		Disposable, FileListener {

	public static final String NAME = "script-store";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Requirement
	private ApplicationConfiguration config;

	private static final String GROOVY = "groovy";

	private static final String DOT_GROOVY = "." + GROOVY;

	private DefaultFileMonitor fileMonitor;

	private Map<String, String> scriptStore;

	private File scriptDir;

	@Override
	public void dispose() {

		fileMonitor.stop();
		fileMonitor = null;

		scriptStore.clear();
		scriptStore = null;

		scriptDir = null;

	}

	@Override
	public void fileChanged(final FileChangeEvent e) throws Exception {
		if (!isScriptFile(e.getFile())) {
			return;
		}

		updateScript(e.getFile());
	}

	@Override
	public void fileCreated(final FileChangeEvent e) throws Exception {
		if (!isScriptFile(e.getFile())) {
			return;
		}

		updateScript(e.getFile());
	}

	@Override
	public void fileDeleted(final FileChangeEvent e) throws Exception {
		if (!isScriptFile(e.getFile())) {
			return;
		}

		synchronized (scriptStore) {
			scriptStore.remove(getName(e.getFile().getName()));
		}
	}

	@Override
	public String getScript(final Class<? extends Event<?>> eventClass) {
		synchronized (scriptStore) {
			return scriptStore.get(eventClass.getName());
		}
	}

	@Override
	public void initialize() throws InitializationException {

		scriptStore = new LinkedHashMap<String, String>();

		FileObject listendir;

		try {

			final FileSystemManager fsManager = VFS.getManager();

			scriptDir = config.getWorkingDirectory("scripts");

			if (!scriptDir.exists()) {

				scriptDir.mkdirs();

				try {

					new File(scriptDir, "place your .groovy files here.txt")
							.createNewFile();

				} catch (final IOException e) {

					throw new InitializationException(e.getMessage(), e);

				}

			}

			listendir = fsManager.resolveFile(scriptDir.getAbsolutePath());

		} catch (final FileSystemException e) {

			throw new InitializationException(e.getMessage(), e);

		}

		final FileSelector selector = new FileSelector() {

			@Override
			public boolean traverseDescendents(final FileSelectInfo arg0)
					throws Exception {
				return true;
			}

			@Override
			public boolean includeFile(final FileSelectInfo arg0)
					throws Exception {
				return isScriptFile(arg0.getFile());
			}

		};

		try {

			final FileObject[] availableScripts = listendir.findFiles(selector);

			for (final FileObject fileObject : availableScripts) {
				updateScript(fileObject);
			}

		} catch (final FileSystemException e) {

			log.warn("Unable to perform initial directory scan.", e);

		}

		final DefaultFileMonitor monitor = new DefaultFileMonitor(this);
		monitor.setRecursive(true);
		monitor.addFile(listendir);
		monitor.start();

		this.fileMonitor = monitor;

	}

	private boolean isScriptFile(final FileObject file) {

		final FileName name = file.getName();

		if (name.getBaseName().endsWith(DOT_GROOVY)) {
			return true;
		}

		return false;

	}

	private void updateScript(final FileObject file) {

		final FileName name = file.getName();

		log.info("New script file found: " + name);

		String script;

		try {

			final FileContent content = file.getContent();

			script = IOUtil.toString(content.getInputStream());

			content.close();

		} catch (final IOException e) {

			log.warn("Unable to read script file: " + name, e);

			return;

		}

		synchronized (scriptStore) {
			scriptStore.put(getName(name), script);
		}

	}

	private String getName(final FileName name) {
		String baseName = name.getBaseName();
		baseName = baseName.substring(0,
				baseName.length() - DOT_GROOVY.length());
		return baseName;
	}

	@Override
	public Map<String, String> getScriptStore() {
		return Collections.unmodifiableMap(this.scriptStore);
	}

	@Override
	public void store(String name, final String script) throws IOException {
		if (!name.endsWith(DOT_GROOVY)) {
			name = name + DOT_GROOVY;
		}

		final File output = new File(scriptDir, name);
		FileUtils.fileWrite(output.getAbsolutePath(), script);

		synchronized (scriptStore) {
			scriptStore.put(name, script);
		}
	}
}
