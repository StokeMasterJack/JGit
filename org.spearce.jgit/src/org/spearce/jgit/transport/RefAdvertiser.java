/*
 * Copyright (C) 2008, 2009, Google Inc.
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

package org.spearce.jgit.transport;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.spearce.jgit.lib.AnyObjectId;
import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.Ref;
import org.spearce.jgit.lib.RefComparator;
import org.spearce.jgit.revwalk.RevFlag;
import org.spearce.jgit.revwalk.RevObject;
import org.spearce.jgit.revwalk.RevTag;
import org.spearce.jgit.revwalk.RevWalk;

/** Support for the start of {@link UploadPack} and {@link ReceivePack}. */
class RefAdvertiser {
	private final PacketLineOut pckOut;

	private final RevWalk walk;

	private final RevFlag ADVERTISED;

	private final StringBuilder tmpLine = new StringBuilder(100);

	private final char[] tmpId = new char[2 * Constants.OBJECT_ID_LENGTH];

	private final Set<String> capablities = new LinkedHashSet<String>();

	private boolean derefTags;

	private boolean first = true;

	RefAdvertiser(final PacketLineOut out, final RevWalk protoWalk,
			final RevFlag advertisedFlag) {
		pckOut = out;
		walk = protoWalk;
		ADVERTISED = advertisedFlag;
	}

	void setDerefTags(final boolean deref) {
		derefTags = deref;
	}

	void advertiseCapability(String name) {
		capablities.add(name);
	}

	void send(final Collection<Ref> refs) throws IOException {
		for (final Ref r : RefComparator.sort(refs)) {
			final RevObject obj = parseAnyOrNull(r.getObjectId());
			if (obj != null) {
				advertiseAny(obj, r.getOrigName());
				if (derefTags && obj instanceof RevTag)
					advertiseTag((RevTag) obj, r.getOrigName() + "^{}");
			}
		}
	}

	void advertiseHave(AnyObjectId id) throws IOException {
		RevObject obj = parseAnyOrNull(id);
		if (obj != null) {
			advertiseAnyOnce(obj, ".have");
			if (obj instanceof RevTag)
				advertiseAnyOnce(((RevTag) obj).getObject(), ".have");
		}
	}

	boolean isEmpty() {
		return first;
	}

	private RevObject parseAnyOrNull(final AnyObjectId id) {
		if (id == null)
			return null;
		try {
			return walk.parseAny(id);
		} catch (IOException e) {
			return null;
		}
	}

	private void advertiseAnyOnce(final RevObject obj, final String refName)
			throws IOException {
		if (!obj.has(ADVERTISED))
			advertiseAny(obj, refName);
	}

	private void advertiseAny(final RevObject obj, final String refName)
			throws IOException {
		obj.add(ADVERTISED);
		advertiseId(obj, refName);
	}

	private void advertiseTag(final RevTag tag, final String refName)
			throws IOException {
		RevObject o = tag;
		do {
			// Fully unwrap here so later on we have these already parsed.
			final RevObject target = ((RevTag) o).getObject();
			try {
				walk.parseHeaders(target);
			} catch (IOException err) {
				return;
			}
			target.add(ADVERTISED);
			o = target;
		} while (o instanceof RevTag);
		advertiseAny(tag.getObject(), refName);
	}

	void advertiseId(final AnyObjectId id, final String refName)
			throws IOException {
		tmpLine.setLength(0);
		id.copyTo(tmpId, tmpLine);
		tmpLine.append(' ');
		tmpLine.append(refName);
		if (first) {
			first = false;
			if (!capablities.isEmpty()) {
				tmpLine.append('\0');
				for (final String capName : capablities) {
					tmpLine.append(' ');
					tmpLine.append(capName);
				}
				tmpLine.append(' ');
			}
		}
		tmpLine.append('\n');
		pckOut.writeString(tmpLine.toString());
	}
}
