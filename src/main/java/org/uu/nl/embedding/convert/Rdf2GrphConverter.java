package org.uu.nl.embedding.convert;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;
import grph.properties.Property;
import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.Settings;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.compare.JaccardSimilarity;
import org.uu.nl.embedding.util.compare.Similarity;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts an RDF graph to the Grph form
 *
 * @author Jurian Baas
 */
public class Rdf2GrphConverter implements Converter<Model, Grph> {

	private static final Logger logger = Logger.getLogger(Rdf2GrphConverter.class);
	private static final Settings settings = Settings.getInstance();

	private final double minSimilarity;
	private final Map<String, Double> weights;

	private static int type2color(Node node) {
		if(node.isURI()) return NodeInfo.URI;
		else if (node.isBlank()) return NodeInfo.BLANK;
		else if (node.isLiteral()) return NodeInfo.LITERAL;
		else throw new IllegalArgumentException("Node " + node + " is not of type URI, blank or literal");
	}

	public Rdf2GrphConverter(Map<String, Double> weights, double minSimilarity) {
		this.weights = weights;
		this.minSimilarity = minSimilarity;
	}

	@Override
	public Grph convert(Model model) {
		
		final Grph g = new InMemoryGrph();

		final Property vertexLabels = g.getVertexLabelProperty();
		final NumericalProperty vertexIsLiteral = g.getVertexShapeProperty();
		final NumericalProperty edgeTypes = g.getEdgeColorProperty();
		final NumericalProperty nodeSimilarity = g.getEdgeWidthProperty();
		
		final Map<Node, Integer> vertexMap = new HashMap<>();
		final Map<Node, Integer> edgeMap = new HashMap<>();

		final Similarity<String> similarityMetric = new JaccardSimilarity(3);

        /*
        final String personURI = "http://goldenagents.org/uva/SAA/ontology/Person";
        final String fullNameURI = "http://goldenagents.org/uva/SAA/ontology/full_name";
		final String altNameURI = "http://goldenagents.org/uva/SAA/ontology/alt_name";
		final String personPartialMatchURI = "http://goldenagents.org/uva/SAA/ontology/Person/partialMatch";

		final List<Resource> personList = new ArrayList<>();

		final Property personProperty = model.createProperty(personURI);
		final Property fullNameProperty = model.createProperty(fullNameURI);
		final Property altNameProperty = model.createProperty(altNameURI);
		final ResIterator personIterator = model.listResourcesWithProperty(RDF.type, personProperty);
		try {
			while(personIterator.hasNext()) {

				Resource person = personIterator.nextResource();
				personList.add(person);

				Statement fullName = person.getProperty(fullNameProperty);
				if(fullName == null || !fullName.getObject().isLiteral()) continue;

				similarityMetric.preprocess(fullName.getLiteral().toString());

			}
		} finally {
			personIterator.close();
		}
		final Map<String, Double> personSimilarityMap = new HashMap<>(personList.size()*personList.size());
		try(ProgressBar pb = settings.progressBar("Comparing", personList.size()*personList.size()/2 - personList.size()/2, "comparisons")) {
			for (int i = 0; i < personList.size(); i++) {

				final Resource r1 = personList.get(i);
				Statement fullName1 = r1.getProperty(fullNameProperty);
				Statement altName1 = r1.getProperty(altNameProperty);

				if (fullName1 == null) continue;

				for (int j = i + 1; j < personList.size(); j++) {
					pb.step();
					final Resource r2 = personList.get(j);

					Statement fullName2 = r2.getProperty(fullNameProperty);
					Statement altName2 = r2.getProperty(altNameProperty);
					if (fullName2 == null) continue;

					//TODO check for logical inconsistencies such as birthDate > deathDate

					final String f1 = fullName1.getLiteral().getString();
					final String f2 = fullName2.getLiteral().getString();

					Property p1 = model.createProperty(
							personPartialMatchURI + "/" + DigestUtils.md5Hex(r1.getURI() + r2.getURI()));
					Property p2 = model.createProperty(
							personPartialMatchURI + "/" + DigestUtils.md5Hex(r2.getURI() + r1.getURI()));

					double similarity = similarityMetric.calculate(f1, f2);
					if (similarity >= minSimilarity) {
						r1.addProperty(p1, r2);
						r2.addProperty(p2, r1);

						personSimilarityMap.put(p1.toString(), similarity);
						personSimilarityMap.put(p2.toString(), similarity);

						continue;
					}

					if(altName2 != null) {
						final String a2 = altName2.getLiteral().getString();
						similarity = similarityMetric.calculate(f1, a2);
						if (similarity >= minSimilarity) {
							r1.addProperty(p1, r2);
							r2.addProperty(p2, r1);

							personSimilarityMap.put(p1.toString(), similarity);
							personSimilarityMap.put(p2.toString(), similarity);

							continue;
						}
					}

					if(altName1 != null) {
						final String a1 = altName1.getLiteral().getString();
						similarity = similarityMetric.calculate(f2, a1);
						if (similarity >= minSimilarity) {

							r1.addProperty(p1, r2);
							r2.addProperty(p2, r1);

							personSimilarityMap.put(p1.toString(), similarity);
							personSimilarityMap.put(p2.toString(), similarity);

							continue;
						}
					}

					if(altName1 != null && altName2 != null) {
						final String a1 = altName1.getLiteral().getString();
						final String a2 = altName2.getLiteral().getString();
						similarity = similarityMetric.calculate(a1, a2);
						if (similarity >= minSimilarity) {

							r1.addProperty(p1, r2);
							r2.addProperty(p2, r1);

							personSimilarityMap.put(p1.toString(), similarity);
							personSimilarityMap.put(p2.toString(), similarity);

						}
					}
				}
			}

		}*/
		int nrOfLiterals = 0;
		int s_i, o_i, p_i, edgeType;
		String predicateString;
		Node s, p, o;
		Triple t   ;

		final ExtendedIterator<Triple> triples = model.getGraph().find();

		try(ProgressBar pb = settings.progressBar("Converting", model.size(), "triples")) {
			while (triples.hasNext()) {

				t = triples.next();
				s = t.getSubject();
				p = t.getPredicate();
				o = t.getObject();

				predicateString = p.toString();

				// Ignore unweighted predicates
				if(!weights.containsKey(predicateString)) {
					// Adjust the total number of triples we are considering
					// Maybe we can do pb.step() here instead to make it less confusing
					pb.maxHint(pb.getMax() - 1);
					continue;
				}

				boolean processLiteralLater = o.isLiteral() && o.getLiteralDatatype().getJavaClass() == String.class && !vertexMap.containsKey(o);

				if(processLiteralLater) {
					similarityMetric.preprocess(o.getLiteralValue().toString());
					nrOfLiterals++;
				}

				// Only create a new ID if the subject is not yet present
				if (!vertexMap.containsKey(s)) addVertex(g, s, vertexMap);
				if (!vertexMap.containsKey(o)) addVertex(g, o, vertexMap);

				s_i = vertexMap.get(s);
				o_i = vertexMap.get(o);

				if(processLiteralLater) {
					vertexIsLiteral.setValue(o_i, 1);
					vertexLabels.setValue(o_i, o.getLiteralValue().toString());
				}

				// Every edge is unique, we always add one to the graph
				// However there are only a few types of edges (relationships)
				// So we also store a reference to a unique edge-type id
				p_i = g.addDirectedSimpleEdge(s_i, o_i);

				g.getEdgeLabelProperty().setValue(p_i, predicateString);

				// If we have not encountered this edge-type before, give it a unique id
				edgeMap.putIfAbsent(p, edgeMap.size());
				// Store the edge-type value for this new edge
				edgeType = edgeMap.get(p);

				edgeTypes.setValue(p_i, edgeType);

				pb.step();
			}
		} finally {
			triples.close();
		}

		int[] vertices = g.getVertices().toIntArray();
		int edgesAdded = 0;
		try(ProgressBar pb = settings.progressBar("Comparing literals", nrOfLiterals*nrOfLiterals/2 - nrOfLiterals/2, "comparisons")) {
			for (int i = 0; i < vertices.length; i++) {

				final int vert = vertices[i];
				if (!vertexIsLiteral.isSetted(vert)) continue;

				for (int j = i + 1; j < vertices.length; j++) {
					final int otherVert = vertices[j];
					if (!vertexIsLiteral.isSetted(otherVert)) continue;

					final String s1 = vertexLabels.getValueAsString(vert);
					final String s2 = vertexLabels.getValueAsString(otherVert);

					final double similarity = similarityMetric.calculate(s1, s2);

					if (similarity >= minSimilarity) {

						byte b = (byte) (similarity * 100);

						int e1 = g.addDirectedSimpleEdge(vert, otherVert);
						int e2 = g.addDirectedSimpleEdge(otherVert, vert);

						nodeSimilarity.setValue(e1, b);
						nodeSimilarity.setValue(e2, b);

						edgesAdded += 2;
					}
					pb.step();
				}
				pb.step();
			}
		}

		logger.info("Added " + edgesAdded + " new edges for " + nrOfLiterals + " literals");

		return g;
	}

	private void addVertex(Grph g, Node n, Map<Node, Integer> vertexMap) {
		final int i = vertexMap.size();
		g.addVertex(i);
		g.getVertexColorProperty().setValue(i, type2color(n));
		g.getVertexLabelProperty().setValue(i, clean(n.toString()));
		vertexMap.put(n, i);
    }

    private String clean(String s) {
	    return s.replace("\"","");
    }

}
