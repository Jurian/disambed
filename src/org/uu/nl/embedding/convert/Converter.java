package org.uu.nl.embedding.convert;

public interface Converter<A, B> {
	public A convert(B b);
}
