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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;

import com.comcast.viper.hlsparserj.tags.media.ExtInf;

public class MediaPlaylistPdtTest {

    @Test
    public void segmentUrisWhenPdtIsBetweenExtInfAndUri() throws Exception {
        MediaPlaylist playlist = parseResource("/mediaplaylist-pdt-between-extinf-and-uri.m3u8");

        assertEquals(3, playlist.getSegments().size());
        assertEquals("segment_001.ts", playlist.getSegments().get(0).getURI());
        assertEquals("segment_002.ts", playlist.getSegments().get(1).getURI());
        assertEquals("segment_003.ts", playlist.getSegments().get(2).getURI());
        assertEquals(17456, playlist.getMediaSequence().getSequenceNumber());
    }

    @Test
    public void segmentUrisWhenPdtIsBeforeExtInf() throws Exception {
        MediaPlaylist playlist = parseResource("/mediaplaylist-pdt-before-extinf.m3u8");

        assertEquals(2, playlist.getSegments().size());
        assertEquals("segment_001.ts", playlist.getSegments().get(0).getURI());
        assertEquals("segment_002.ts", playlist.getSegments().get(1).getURI());
    }

    @Test
    public void segmentUrisWhenLinesHaveLeadingOrTrailingWhitespace() {
        String playlist = "#EXTM3U\n"
            + "#EXT-X-TARGETDURATION:4\n"
            + "#EXT-X-MEDIA-SEQUENCE:0\n"
            + " #EXTINF:4, \n"
            + "  segment_001.ts  \n"
            + "\n"
            + "   \n"
            + " #EXTINF:4,\n"
            + "segment_002.ts\r\n";

        MediaPlaylist parsed = (MediaPlaylist) PlaylistFactory.parsePlaylist(
            PlaylistVersion.TWELVE, playlist);

        assertEquals(2, parsed.getSegments().size());
        assertEquals("segment_001.ts", parsed.getSegments().get(0).getURI());
        assertEquals("segment_002.ts", parsed.getSegments().get(1).getURI());
    }

    @Test
    public void segmentUrisUnchangedForStandardPlaylist() throws Exception {
        MediaPlaylist playlist = parseResource("/mediaplaylist.m3u8");

        assertNotNull(playlist.getSegments().get(0).getURI());
        assertEquals("20140311T113819-01-338559live.ts", playlist.getSegments().get(0).getURI());
    }

    private static MediaPlaylist parseResource(final String resource) throws Exception {
        try (InputStream in = MediaPlaylistPdtTest.class.getResourceAsStream(resource)) {
            return (MediaPlaylist) PlaylistFactory.parsePlaylist(PlaylistVersion.TWELVE, in);
        }
    }
}
