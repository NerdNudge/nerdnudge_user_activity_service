package com.neurospark.nerdnudge.useractivity.utils;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K,T> extends LinkedHashMap<K,T> {
	private static final long serialVersionUID = 1L;
	private int capacity;

	public LRUCache(int capacity, float d ){
		super( capacity, d, true );
		this.capacity = capacity;
	}

	@Override
	protected boolean removeEldestEntry( Map.Entry< K, T > eldest ) {
		return size() > this.capacity;
	}


	public static void main( ) {
		LRUCache< String, JsonObject> cache = new LRUCache< String, JsonObject >( 100, 0.5f );
		cache.put( "test@gmail.com", new JsonObject() );
	}
}
