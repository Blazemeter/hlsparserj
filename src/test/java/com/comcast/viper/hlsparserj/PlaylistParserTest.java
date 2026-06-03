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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class PlaylistParserTest {

    private static final String MASTER_PLAYLIST = "#EXTM3U\n"
        + "#EXT-X-STREAM-INF:BANDWIDTH=1280000\n"
        + "variant.m3u8\n";

    private static final String MEDIA_PLAYLIST = "#EXTM3U\n"
        + "#EXT-X-TARGETDURATION:4\n"
        + "#EXTINF:4,\n"
        + "segment.ts\n";

    @Test
    public void reusingParserClearsStateBetweenStringParses() {
        PlaylistParser parser = new PlaylistParser();

        parser.parse(MASTER_PLAYLIST);
        assertTrue(parser.isMasterPlaylist());
        assertEquals(2, parser.getTags().size());

        parser.parse(MEDIA_PLAYLIST);
        assertFalse(parser.isMasterPlaylist());
        assertEquals(3, parser.getTags().size());
    }

    @Test
    public void reusingParserClearsStateBetweenStreamParses() throws Exception {
        PlaylistParser parser = new PlaylistParser();

        parser.parse(new ByteArrayInputStream(MASTER_PLAYLIST.getBytes(StandardCharsets.UTF_8)));
        assertTrue(parser.isMasterPlaylist());

        parser.parse(new ByteArrayInputStream(MEDIA_PLAYLIST.getBytes(StandardCharsets.UTF_8)));
        assertFalse(parser.isMasterPlaylist());
        assertEquals(3, parser.getTags().size());
    }

    @Test
    public void reusingParserClearsStateBetweenStringAndStreamParses() throws Exception {
        PlaylistParser parser = new PlaylistParser();

        parser.parse(MASTER_PLAYLIST);
        assertTrue(parser.isMasterPlaylist());

        parser.parse(new ByteArrayInputStream(MEDIA_PLAYLIST.getBytes(StandardCharsets.UTF_8)));
        assertFalse(parser.isMasterPlaylist());
        assertEquals(3, parser.getTags().size());
    }
}
