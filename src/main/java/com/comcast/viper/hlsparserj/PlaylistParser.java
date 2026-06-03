/**
 * Copyright 2015 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.comcast.viper.hlsparserj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.comcast.viper.hlsparserj.tags.TagNames;
import com.comcast.viper.hlsparserj.tags.UnparsedTag;

/**
 * Class to parse playlists.  Capable to parse playlist string or inputStream.
 */
public class PlaylistParser {

    private InputStream playlistStream;

    private final List<UnparsedTag> tags;

    private static final String TAGPATTERN = "^#EXT.*";
    private static final String URIPATTERN = "^[^#].*";

    private boolean isMasterPlaylist = false;

    /**
     * Tag that should receive the next media segment URI line ({@link TagNames#EXTINF}
     * or {@link TagNames#EXTXBYTERANGE}). Other tags (e.g. program date/time) may appear
     * between that tag and the URI without claiming it.
     */
    private UnparsedTag lastSegmentUriTag;

    /**
     * Constructor.
     */
    public PlaylistParser() {
        tags = new ArrayList<UnparsedTag>();
    }

    /**
     * Parse a given playlist string.
     * @param playlist playlist string
     */
    public void parse(final String playlist) {
        parseString(playlist);
    }

    /**
     * Parse an inputStream of a correctly formatted playlist.
     * @param inputStream inputStream
     * @throws IOException on connection and parsing exception
     */
    public void parse(final InputStream inputStream) throws IOException {
        this.playlistStream = inputStream;
        parseInputStream();
    }

    /**
     * Returns boolean to indicate if the playlist is a master playlist.
     * @return boolean
     */
    public boolean isMasterPlaylist() {
        return isMasterPlaylist;
    }

    /**
     * Returns list of unparsed tags.
     * @return list of tags
     */
    public List<UnparsedTag> getTags() {
        return tags;
    }

    /**
     * Parse a given playlist string.
     * @param playlist playlist string
     */
    private void parseString(final String playlist) {
        lastSegmentUriTag = null;
        final StringTokenizer tokenizer = new StringTokenizer(playlist, "\n");

        String line;
        UnparsedTag lastTag = null;
        while (tokenizer.hasMoreElements()) {
            line = tokenizer.nextToken();
            lastTag = processLine(line, lastTag);
        }
    }

    /**
     * Parse a given inputStream to a valid playlist.
     * @throws IOException on reading the inputStream
     */
    private void parseInputStream() throws IOException {
        lastSegmentUriTag = null;
        final InputStreamReader isReader = new InputStreamReader(playlistStream);
        final BufferedReader bufReader = new BufferedReader(isReader);

        UnparsedTag lastTag = null;
        String line;
        while ((line = bufReader.readLine()) != null) {
            lastTag = processLine(line, lastTag);
        }
    }

    /**
     * Parse a given string line from the playlist.
     *
     * If the line is prefixed with a "#", it is handled as a new tag and
     * parsed/added to the list of tags.
     *
     * If the line is a URI, it is set on the pending segment tag ({@link TagNames#EXTINF}
     * or {@link TagNames#EXTXBYTERANGE}) when present; otherwise on the last tag (e.g.
     * master playlist variant URIs after {@link TagNames#EXTXSTREAMINF}).
     *
     * @param line playlist line item
     * @param lastTag last tag
     * @return unparsed tag
     */
    private UnparsedTag processLine(final String line, final UnparsedTag lastTag) {
        final String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return lastTag;
        }

        if (trimmed.matches(TAGPATTERN)) {
            final UnparsedTag newUnparsedTag = new UnparsedTag(trimmed);
            tags.add(newUnparsedTag);

            // Check if this tag specifies a variant stream. If so, this is
            // a master playlist
            if (newUnparsedTag.getTagName().equals(TagNames.EXTXSTREAMINF)) {
                this.isMasterPlaylist = true;
            }

            final String tagName = newUnparsedTag.getTagName();
            if (tagName.equals(TagNames.EXTINF) || tagName.equals(TagNames.EXTXBYTERANGE)) {
                lastSegmentUriTag = newUnparsedTag;
            }

            return newUnparsedTag;
        } else if (trimmed.matches(URIPATTERN)) {
            final UnparsedTag uriTag = lastSegmentUriTag != null ? lastSegmentUriTag : lastTag;
            if (uriTag != null) {
                uriTag.setURI(trimmed);
                lastSegmentUriTag = null;
            }
            return lastTag;
        }

        // Unexpected situation
        return lastTag;
    }
}
