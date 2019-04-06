package org.uu.nl.embedding.convert;

/**
 * @author Jurian Baas
 * @param <A> To
 * @param <B> From
 */
interface Converter<B, A> {
	A convert(B b);
}
