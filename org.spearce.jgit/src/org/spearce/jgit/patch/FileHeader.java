import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

	/** Type of patch used by this file. */
	public static enum PatchType {
		/** A traditional unified diff style patch of a text file. */
		UNIFIED,

		/** An empty patch with a message "Binary files ... differ" */
		BINARY,

		/** A Git binary patch, holding pre and post image deltas */
		GIT_BINARY;
	}

	/** Type of patch used to modify this file */
	PatchType patchType;

	/** The hunks of this file */
	private List<HunkHeader> hunks;

	/** If {@link #patchType} is {@link PatchType#GIT_BINARY}, the new image */
	BinaryHunk forwardBinaryHunk;

	/** If {@link #patchType} is {@link PatchType#GIT_BINARY}, the old image */
	BinaryHunk reverseBinaryHunk;

		patchType = PatchType.UNIFIED;
	/** @return style of patch used to modify this file */
	public PatchType getPatchType() {
		return patchType;
	}

	/** @return true if this patch modifies metadata about a file */
	public boolean hasMetaDataChanges() {
		return changeType != ChangeType.MODIFY || newMode != oldMode;
	}

	/** @return hunks altering this file; in order of appearance in patch */
	public List<HunkHeader> getHunks() {
		if (hunks == null)
			return Collections.emptyList();
		return hunks;
	}

	void addHunk(final HunkHeader h) {
		if (h.getFileHeader() != this)
			throw new IllegalArgumentException("Hunk belongs to another file");
		if (hunks == null)
			hunks = new ArrayList<HunkHeader>();
		hunks.add(h);
	}

	/** @return if a {@link PatchType#GIT_BINARY}, the new-image delta/literal */
	public BinaryHunk getForwardBinaryHunk() {
		return forwardBinaryHunk;
	}

	/** @return if a {@link PatchType#GIT_BINARY}, the old-image delta/literal */
	public BinaryHunk getReverseBinaryHunk() {
		return reverseBinaryHunk;
	}

	 * @param end
	 *            one past the last position to parse.
	int parseGitFileName(int ptr, final int end) {
		if (eol >= end) {
	int parseGitHeaders(int ptr, final int end) {
		while (ptr < end) {
			if (isHunkHdr(buf, ptr, eol) >= 1) {
	int parseTraditionalHeaders(int ptr, final int end) {
		while (ptr < end) {
			if (isHunkHdr(buf, ptr, eol) >= 1) {

	/**
	 * Determine if this is a patch hunk header.
	 *
	 * @param buf
	 *            the buffer to scan
	 * @param start
	 *            first position in the buffer to evaluate
	 * @param end
	 *            last position to consider; usually the end of the buffer (
	 *            <code>buf.length</code>) or the first position on the next
	 *            line. This is only used to avoid very long runs of '@' from
	 *            killing the scan loop.
	 * @return the number of "ancestor revisions" in the hunk header. A
	 *         traditional two-way diff ("@@ -...") returns 1; a combined diff
	 *         for a 3 way-merge returns 3. If this is not a hunk header, 0 is
	 *         returned instead.
	 */
	static int isHunkHdr(final byte[] buf, final int start, final int end) {
		int ptr = start;
		while (ptr < end && buf[ptr] == '@')
			ptr++;
		if (ptr - start < 2)
			return 0;
		if (ptr == end || buf[ptr++] != ' ')
			return 0;
		if (ptr == end || buf[ptr++] != '-')
			return 0;
		return (ptr - 3) - start;
	}