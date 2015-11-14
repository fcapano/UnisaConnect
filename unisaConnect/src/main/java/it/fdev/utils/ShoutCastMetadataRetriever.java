/*
 * ServeStream: A HTTP stream browser/player for Android
 * Copyright 2013 William Seemann
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

package it.fdev.utils;

import net.moraleboost.streamscraper.ScrapeException;
import net.moraleboost.streamscraper.Scraper;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.scraper.IceCastScraper;
import net.moraleboost.streamscraper.scraper.ShoutCastScraper;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * ShoutCastMetadataRetriever class provides a unified interface for retrieving
 * meta data from an input media file.
 */
public class ShoutCastMetadataRetriever
{
	private URI uri;
	private Scraper scraper;
	
    public ShoutCastMetadataRetriever(String path) throws URISyntaxException, ScrapeException {
    	uri = new URI(path);
    	
    	try {
    		scraper = new ShoutCastScraper();
    		getMetadata();
    		return;
    	} catch (ScrapeException ex) {
    	} catch (NullPointerException ex) {
    	}
    	
    	try {
    		scraper = new IceCastScraper();
    		getMetadata();
    		return;
    	} catch (ScrapeException ex) {
    	} catch (NullPointerException ex) {
    	}
    	
    	throw new ScrapeException();
    }

    /**
     * Retrieves metadata from a specified path.
     * 
     * @param path The path of the input media file
     * @param scraper The scraper
     * @throws URISyntaxException If the path cannot be parsed. 
     * @throws ScrapeException If metadata cannot be retrieved.
     */
    public String getMetadata() throws URISyntaxException, ScrapeException {
    	Stream stream = scraper.scrape(uri).get(0);
    	return stream.getCurrentSong();
    	// No exception happened
	}
    
}
