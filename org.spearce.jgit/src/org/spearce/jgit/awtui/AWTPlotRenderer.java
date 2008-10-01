/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
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

package org.spearce.jgit.awtui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import org.spearce.jgit.awtui.CommitGraphPane.GraphCellRender;
import org.spearce.jgit.awtui.SwingCommitList.SwingLane;
import org.spearce.jgit.revplot.AbstractPlotRenderer;
import org.spearce.jgit.revplot.PlotCommit;

final class AWTPlotRenderer extends AbstractPlotRenderer<SwingLane, Color> {

	final GraphCellRender cell;

	Graphics2D g;

	AWTPlotRenderer(final GraphCellRender c) {
		cell = c;
	}

	void paint(final Graphics in, final PlotCommit<SwingLane> commit) {
		g = (Graphics2D) in.create();
		try {
			final int h = cell.getHeight();
			g.setColor(cell.getBackground());
			g.fillRect(0, 0, cell.getWidth(), h);
			if (commit != null)
				paintCommit(commit, h);
		} finally {
			g.dispose();
			g = null;
		}
	}

	@Override
	protected void drawLine(final Color color, int x1, int y1, int x2,
			int y2, int width) {
		if (y1 == y2) {
			x1 -= width / 2;
			x2 -= width / 2;
		} else if (x1 == x2) {
			y1 -= width / 2;
			y2 -= width / 2;
		}

		g.setColor(color);
		g.setStroke(CommitGraphPane.stroke(width));
		g.drawLine(x1, y1, x2, y2);
	}

	@Override
	protected void drawCommitDot(final int x, final int y, final int w,
			final int h) {
		g.setColor(Color.blue);
		g.setStroke(CommitGraphPane.strokeCache[1]);
		g.fillOval(x, y, w, h);
		g.setColor(Color.black);
		g.drawOval(x, y, w, h);
	}

	@Override
	protected void drawBoundaryDot(final int x, final int y, final int w,
			final int h) {
		g.setColor(cell.getBackground());
		g.setStroke(CommitGraphPane.strokeCache[1]);
		g.fillOval(x, y, w, h);
		g.setColor(Color.black);
		g.drawOval(x, y, w, h);
	}

	@Override
	protected void drawText(final String msg, final int x, final int y) {
		final int texty = g.getFontMetrics().getHeight()
				- g.getFontMetrics().getDescent();
		g.setColor(cell.getForeground());
		g.drawString(msg, x, texty - (cell.getHeight() - y * 2));
	}

	@Override
	protected Color laneColor(final SwingLane myLane) {
		return myLane != null ? myLane.color : Color.black;
	}

	void paintTriangleDown(final int cx, final int y, final int h) {
		final int tipX = cx;
		final int tipY = y + h;
		final int baseX1 = cx - 10 / 2;
		final int baseX2 = tipX + 10 / 2;
		final int baseY = y;
		final Polygon triangle = new Polygon();
		triangle.addPoint(tipX, tipY);
		triangle.addPoint(baseX1, baseY);
		triangle.addPoint(baseX2, baseY);
		g.fillPolygon(triangle);
		g.drawPolygon(triangle);
	}

}