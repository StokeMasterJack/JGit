/*
 * Copyright (C) 2008, Google Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Git Development Community nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spearce.jgit.patch;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.spearce.jgit.lib.FileMode;

public class PatchTest extends TestCase {
	public void testEmpty() {
		final Patch p = new Patch();
		assertTrue(p.getFiles().isEmpty());
	}

	public void testParse_ConfigCaseInsensitive() throws IOException {
		final Patch p = parseTestPatchFile();
		assertEquals(2, p.getFiles().size());

		final FileHeader fRepositoryConfigTest = p.getFiles().get(0);
		final FileHeader fRepositoryConfig = p.getFiles().get(1);

		assertEquals(
				"org.spearce.jgit.test/tst/org/spearce/jgit/lib/RepositoryConfigTest.java",
				fRepositoryConfigTest.getNewName());

		assertEquals(
				"org.spearce.jgit/src/org/spearce/jgit/lib/RepositoryConfig.java",
				fRepositoryConfig.getNewName());

		assertEquals(572, fRepositoryConfigTest.startOffset);
		assertEquals(1490, fRepositoryConfig.startOffset);

		assertEquals("da7e704", fRepositoryConfigTest.getOldId().name());
		assertEquals("34ce04a", fRepositoryConfigTest.getNewId().name());
		assertSame(FileMode.REGULAR_FILE, fRepositoryConfigTest.getOldMode());
		assertSame(FileMode.REGULAR_FILE, fRepositoryConfigTest.getNewMode());

		assertEquals("45c2f8a", fRepositoryConfig.getOldId().name());
		assertEquals("3291bba", fRepositoryConfig.getNewId().name());
		assertSame(FileMode.REGULAR_FILE, fRepositoryConfig.getOldMode());
		assertSame(FileMode.REGULAR_FILE, fRepositoryConfig.getNewMode());
	}

	private Patch parseTestPatchFile() throws IOException {
		final String patchFile = getName() + ".patch";
		final InputStream in = getClass().getResourceAsStream(patchFile);
		if (in == null) {
			fail("No " + patchFile + " test vector");
			return null; // Never happens
		}
		try {
			final Patch p = new Patch();
			p.parse(in);
			return p;
		} finally {
			in.close();
		}
	}
}