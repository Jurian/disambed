package org.uu.nl.convert;

public interface Converter<A, B> {
	public A convert(B b);
}
