package edu.kit.ipd.parse.graphBuilder;

import java.util.List;
import java.util.Properties;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.data.AbstractPipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PipelineDataCastException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.data.token.AlternativeHypothesisToken;
import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.pipeline.IPipelineStage;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.ConfigManager;

@MetaInfServices(IPipelineStage.class)
public class GraphBuilder implements IPipelineStage {

	private static final Logger logger = LoggerFactory.getLogger(GraphBuilder.class);

	private static final String ID = "graphBuilder";

	private static final String RELATION_ARC_TYPE = "relation";
	private static final String TOKEN_NODE_TYPE = "token";
	private static final String ALTERNATIVE_ARC_TYPE = "alternative";
	private static final String ALTERNATIVE_TOKEN_NODE_TYPE = "alternative_token";

	private PrePipelineData prePipeData;

	private Properties props;

	private static final String MULTIPLE_GRAPHS_PROP = "MULTIPLE_GRAPHS";

	private boolean multipleGraphs;

	@Override
	public void init() {
		props = ConfigManager.getConfiguration(getClass());
		multipleGraphs = Boolean.getBoolean(props.getProperty(MULTIPLE_GRAPHS_PROP)) ? true : false;
	}

	@Override
	public void exec(AbstractPipelineData data) throws PipelineStageException {
		// try to get data as pre pipeline data. If this fails, return
		try {
			prePipeData = data.asPrePipelineData();
		} catch (final PipelineDataCastException e) {
			logger.error("Cannot process on data - PipelineData unreadable", e);
			throw new PipelineStageException(e);
		}

		if (multipleGraphs) {
			// TODO: implementation for multiple graphs
		} else {
			try {
				final List<Token> tokens = prePipeData.getTaggedHypothesis(0);
				prePipeData.setGraph(generateGraphFromTokens(tokens));
			} catch (final MissingDataException e) {
				logger.error("No main tagged hypothesis given!", e);
				throw new PipelineStageException(e);
			}
		}

	}

	private IGraph generateGraphFromTokens(List<Token> tokens) {
		final IGraph graph = new ParseGraph();
		INodeType wordType, altType;
		IArcType arcType, altRelType;

		//get or create the types
		if (graph.hasNodeType(TOKEN_NODE_TYPE)) {
			wordType = graph.getNodeType(TOKEN_NODE_TYPE);
		} else {
			wordType = graph.createNodeType(TOKEN_NODE_TYPE);
		}

		if (graph.hasArcType(RELATION_ARC_TYPE)) {
			arcType = graph.getArcType(RELATION_ARC_TYPE);
		} else {
			arcType = graph.createArcType(RELATION_ARC_TYPE);
		}

		if (graph.hasNodeType(ALTERNATIVE_TOKEN_NODE_TYPE)) {
			altType = graph.getNodeType(ALTERNATIVE_TOKEN_NODE_TYPE);
		} else {
			altType = graph.createNodeType(ALTERNATIVE_TOKEN_NODE_TYPE);
		}

		if (graph.hasArcType(ALTERNATIVE_ARC_TYPE)) {
			altRelType = graph.getArcType(ALTERNATIVE_ARC_TYPE);
		} else {
			altRelType = graph.createArcType(ALTERNATIVE_ARC_TYPE);
		}

		//TODO: make the following variable, e.g., wrap attributes of the token in an object with iterable elements

		wordType.addAttributeToType("String", "value");
		wordType.addAttributeToType("String", "pos");
		wordType.addAttributeToType("String", "chunkIOB");
		wordType.addAttributeToType("String", "chunkName");
		wordType.addAttributeToType("String", "type");
		wordType.addAttributeToType("int", "predecessors");
		wordType.addAttributeToType("int", "successors");
		wordType.addAttributeToType("int", "instructionNumber");
		wordType.addAttributeToType("int", "position");
		wordType.addAttributeToType("double", "asrConfidence");
		wordType.addAttributeToType("int", "alternativesCount");
		wordType.addAttributeToType("double", "startTime");
		wordType.addAttributeToType("double", "endTime");

		arcType.addAttributeToType("String", "value");

		altType.addAttributeToType("String", "value");
		altType.addAttributeToType("String", "type");
		altType.addAttributeToType("int", "position");
		altType.addAttributeToType("float", "asrConfidence");
		altType.addAttributeToType("float", "startTime");
		altType.addAttributeToType("float", "endTime");

		altRelType.addAttributeToType("int", "number");

		INode lastNode = null;
		for (final Token tok : tokens) {
			final INode node = graph.createNode(wordType);
			node.setAttributeValue("value", tok.getWord());
			node.setAttributeValue("pos", tok.getPos().toString());
			node.setAttributeValue("chunkIOB", tok.getChunkIOB().toString());
			node.setAttributeValue("chunkName", tok.getChunk().getName());
			node.setAttributeValue("predecessors", tok.getChunk().getPredecessor());
			node.setAttributeValue("successors", tok.getChunk().getSuccessor());
			node.setAttributeValue("instructionNumber", tok.getInstructionNumber());
			node.setAttributeValue("position", tok.getPosition());
			node.setAttributeValue("type", tok.getType().toString());
			node.setAttributeValue("asrConfidence", tok.getConfidence());
			node.setAttributeValue("startTime", tok.getStartTime());
			node.setAttributeValue("endTime", tok.getEndTime());
			node.setAttributeValue("alternativesCount", tok.getAlternatives().size());

			for (int i = 0; i < tok.getAlternatives().size(); i++) {
				final AlternativeHypothesisToken altHyp = tok.getAlternative(i);
				final INode alternativeToken = graph.createNode(altType);
				alternativeToken.setAttributeValue("value", altHyp.getWord());
				alternativeToken.setAttributeValue("position", altHyp.getPosition());
				alternativeToken.setAttributeValue("type", altHyp.getType().toString());
				alternativeToken.setAttributeValue("asrConfidence", altHyp.getConfidence());
				alternativeToken.setAttributeValue("startTime", altHyp.getStartTime());
				alternativeToken.setAttributeValue("endTime", altHyp.getEndTime());
				final IArc altRel = graph.createArc(node, alternativeToken, arcType);
				altRel.setAttributeValue("number", i);
			}

			//add arcs between main hyp tokens
			if (lastNode != null) {
				final IArc arc = graph.createArc(lastNode, node, arcType);
				arc.setAttributeValue("value", "NEXT");
				lastNode = node;
			}
		}
		return graph;
	}

	@Override
	public String getID() {
		return ID;
	}

}
