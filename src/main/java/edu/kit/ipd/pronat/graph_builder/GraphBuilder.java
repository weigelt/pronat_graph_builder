package edu.kit.ipd.pronat.graph_builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import edu.kit.ipd.pronat.prepipedatamodel.PrePipelineData;
import edu.kit.ipd.pronat.prepipedatamodel.token.AlternativeHypothesisToken;
import edu.kit.ipd.pronat.prepipedatamodel.token.SRLToken;
import edu.kit.ipd.pronat.prepipedatamodel.token.Token;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.data.AbstractPipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PipelineDataCastException;
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

	public static final String EVENT_TYPES_ATTRIBUTE = "eventTypes";
	public static final String FRAME_NET_FRAMES_ATTRIBUTE = "frameNetFrames";
	public static final String VERB_NET_FRAMES_ATTRIBUTE = "verbNetFrames";
	public static final String PROP_BANK_ROLESET_DESCR_ATTRIBUTE = "propBankRolesetDescr";
	public static final String PROP_BANK_ROLESET_ID_ATTRIBUTE = "propBankRolesetID";
	public static final String CORRESPONDING_VERB_ATTRIBUTE = "correspondingVerb";
	public static final String ROLE_CONFIDENCE_ATTRIBUTE = "roleConfidence";
	public static final String FN_ROLE_ATTRIBUTE = "fnRole";
	public static final String VN_ROLE_ATTRIBUTE = "vnRole";
	public static final String PB_ROLE_ATTRIBUTE = "pbRole";
	public static final String ROLE_ATTRIBUTE = "role";
	public static final String NUMBER_ATTRIBUTE = "number";
	public static final String SENTENCE_NUMBER_ATTRIBUTE = "sentenceNumber";
	public static final String VERIFIED_BY_DIALOG_AGENT_ATTRIBUTE = "verifiedByDialogAgent";
	public static final String STEM_ATTRIBUTE = "stem";
	public static final String LEMMA_ATTRIBUTE = "lemma";
	public static final String NER_ATTRIBUTE = "ner";
	public static final String END_TIME_ATTRIBUTE = "endTime";
	public static final String START_TIME_ATTRIBUTE = "startTime";
	public static final String ALTERNATIVES_COUNT_ATTRIBUTE = "alternativesCount";
	public static final String ASR_CONFIDENCE_ATTRIBUTE = "asrConfidence";
	public static final String POSITION_ATTRIBUTE = "position";
	public static final String INSTRUCTION_NUMBER_ATTRIBUTE = "instructionNumber";
	public static final String SUCCESSORS_ATTRIBUTE = "successors";
	public static final String PREDECESSORS_ATTRIBUTE = "predecessors";
	public static final String TYPE_ATTRIBUTE = "type";
	public static final String CHUNK_ATTRIBUTE = "chunkName";
	public static final String CHUNK_IOB_ATTRIBUTE = "chunkIOB";
	public static final String POS_ATTRIBUTE = "pos";
	public static final String WORD_ATTRIBUTE = "value";
	public static final String VALUE_ATTRIBUTE = "value";

	private static final Logger logger = LoggerFactory.getLogger(GraphBuilder.class);

	private static final String ID = "graphBuilder";

	private static final String RELATION_ARC_TYPE = "relation";
	private static final String TOKEN_NODE_TYPE = "token";
	private static final String ALTERNATIVE_ARC_TYPE = "alternative";
	private static final String ALTERNATIVE_TOKEN_NODE_TYPE = "alternative_token";
	private static final String SRL_ARC_TYPE = "srl";

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
			prePipeData = (PrePipelineData) data.asPrePipelineData();
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

		wordType.addAttributeToType("String", WORD_ATTRIBUTE);
		wordType.addAttributeToType("String", POS_ATTRIBUTE);
		wordType.addAttributeToType("String", CHUNK_IOB_ATTRIBUTE);
		wordType.addAttributeToType("String", CHUNK_ATTRIBUTE);
		wordType.addAttributeToType("String", TYPE_ATTRIBUTE);
		wordType.addAttributeToType("int", PREDECESSORS_ATTRIBUTE);
		wordType.addAttributeToType("int", SUCCESSORS_ATTRIBUTE);
		wordType.addAttributeToType("int", INSTRUCTION_NUMBER_ATTRIBUTE);
		wordType.addAttributeToType("int", POSITION_ATTRIBUTE);
		wordType.addAttributeToType("double", ASR_CONFIDENCE_ATTRIBUTE);
		wordType.addAttributeToType("int", ALTERNATIVES_COUNT_ATTRIBUTE);
		wordType.addAttributeToType("double", START_TIME_ATTRIBUTE);
		wordType.addAttributeToType("double", END_TIME_ATTRIBUTE);
		wordType.addAttributeToType("String", NER_ATTRIBUTE);
		wordType.addAttributeToType("String", LEMMA_ATTRIBUTE);
		wordType.addAttributeToType("String", STEM_ATTRIBUTE);
		wordType.addAttributeToType("boolean", VERIFIED_BY_DIALOG_AGENT_ATTRIBUTE);
		wordType.addAttributeToType("int", SENTENCE_NUMBER_ATTRIBUTE);

		arcType.addAttributeToType("String", VALUE_ATTRIBUTE);

		altType.addAttributeToType("String", WORD_ATTRIBUTE);
		altType.addAttributeToType("String", TYPE_ATTRIBUTE);
		altType.addAttributeToType("int", POSITION_ATTRIBUTE);
		altType.addAttributeToType("double", ASR_CONFIDENCE_ATTRIBUTE);
		altType.addAttributeToType("double", START_TIME_ATTRIBUTE);
		altType.addAttributeToType("double", END_TIME_ATTRIBUTE);

		altRelType.addAttributeToType("int", NUMBER_ATTRIBUTE);

		Map<Token, INode> nodesForTokens = new TreeMap<>();
		HashSet<SRLToken> srlTokens = new HashSet<>();

		INode lastNode = null;
		for (final Token tok : tokens) {
			final INode node = graph.createNode(wordType);
			node.setAttributeValue(WORD_ATTRIBUTE, tok.getWord());
			node.setAttributeValue(POS_ATTRIBUTE, tok.getPos().toString());
			node.setAttributeValue(CHUNK_IOB_ATTRIBUTE, tok.getChunkIOB().toString());
			node.setAttributeValue(CHUNK_ATTRIBUTE, tok.getChunk().getName());
			node.setAttributeValue(PREDECESSORS_ATTRIBUTE, tok.getChunk().getPredecessor());
			node.setAttributeValue(SUCCESSORS_ATTRIBUTE, tok.getChunk().getSuccessor());
			node.setAttributeValue(INSTRUCTION_NUMBER_ATTRIBUTE, tok.getInstructionNumber());
			node.setAttributeValue(POSITION_ATTRIBUTE, tok.getPosition());
			node.setAttributeValue(TYPE_ATTRIBUTE, tok.getType().toString());
			node.setAttributeValue(ASR_CONFIDENCE_ATTRIBUTE, tok.getConfidence());
			node.setAttributeValue(START_TIME_ATTRIBUTE, tok.getStartTime());
			node.setAttributeValue(END_TIME_ATTRIBUTE, tok.getEndTime());
			node.setAttributeValue(ALTERNATIVES_COUNT_ATTRIBUTE, tok.getAlternatives().size());
			node.setAttributeValue(NER_ATTRIBUTE, tok.getNer());
			node.setAttributeValue(LEMMA_ATTRIBUTE, tok.getLemma());
			node.setAttributeValue(STEM_ATTRIBUTE, tok.getStem());
			node.setAttributeValue(VERIFIED_BY_DIALOG_AGENT_ATTRIBUTE, false);
			node.setAttributeValue(SENTENCE_NUMBER_ATTRIBUTE, tok.getSentenceNumber());

			nodesForTokens.put(tok, node);
			for (int i = 0; i < tok.getAlternatives().size(); i++) {
				final AlternativeHypothesisToken altHyp = tok.getAlternative(i);
				final INode alternativeToken = graph.createNode(altType);
				alternativeToken.setAttributeValue(WORD_ATTRIBUTE, altHyp.getWord());
				alternativeToken.setAttributeValue(POSITION_ATTRIBUTE, altHyp.getPosition());
				alternativeToken.setAttributeValue(TYPE_ATTRIBUTE, altHyp.getType().toString());
				alternativeToken.setAttributeValue(ASR_CONFIDENCE_ATTRIBUTE, altHyp.getConfidence());
				alternativeToken.setAttributeValue(START_TIME_ATTRIBUTE, altHyp.getStartTime());
				alternativeToken.setAttributeValue(END_TIME_ATTRIBUTE, altHyp.getEndTime());
				final IArc altRel = graph.createArc(node, alternativeToken, altRelType);
				altRel.setAttributeValue(NUMBER_ATTRIBUTE, i);
			}

			//add arcs between main hyp tokens
			if (lastNode != null) {
				final IArc arc = graph.createArc(lastNode, node, arcType);
				arc.setAttributeValue(WORD_ATTRIBUTE, "NEXT");
			}

			if (tok instanceof SRLToken) {
				srlTokens.add((SRLToken) tok);
			}

			lastNode = node;
		}

		if (!srlTokens.isEmpty()) {
			IArcType srlArcType;

			if (graph.hasArcType(SRL_ARC_TYPE)) {
				srlArcType = graph.getArcType(SRL_ARC_TYPE);
			} else {
				srlArcType = graph.createArcType(SRL_ARC_TYPE);
				srlArcType.addAttributeToType("String", ROLE_ATTRIBUTE);
				srlArcType.addAttributeToType("String", PB_ROLE_ATTRIBUTE);
				srlArcType.addAttributeToType("String", VN_ROLE_ATTRIBUTE);
				srlArcType.addAttributeToType("String", FN_ROLE_ATTRIBUTE);
				srlArcType.addAttributeToType("Double", ROLE_CONFIDENCE_ATTRIBUTE);
				srlArcType.addAttributeToType("String", CORRESPONDING_VERB_ATTRIBUTE);
				srlArcType.addAttributeToType("String", PROP_BANK_ROLESET_ID_ATTRIBUTE);
				srlArcType.addAttributeToType("String", PROP_BANK_ROLESET_DESCR_ATTRIBUTE);
				srlArcType.addAttributeToType("String", VERB_NET_FRAMES_ATTRIBUTE);
				srlArcType.addAttributeToType("String", FRAME_NET_FRAMES_ATTRIBUTE);
				srlArcType.addAttributeToType("String", EVENT_TYPES_ATTRIBUTE);
			}

			for (SRLToken srlToken : srlTokens) {
				createSRLArcs(srlToken, srlArcType, nodesForTokens, graph);
			}
		}
		return graph;
	}

	private void createSRLArcs(SRLToken srlToken, IArcType srlArcType, Map<Token, INode> nodesForTokens, IGraph graph) {
		Token last = srlToken;
		for (Token verb : srlToken.getVerbTokens()) {
			IArc arc = graph.createArc(nodesForTokens.get(last), nodesForTokens.get(verb), srlArcType);
			setSharedSRLArcInfos(srlToken, arc, "V");
			last = verb;
		}
		for (String role : srlToken.getDependentRoles()) {
			if (!role.equals("V")) {
				last = srlToken;
				for (Token roleToken : srlToken.getTokensOfRole(role)) {
					IArc arc = graph.createArc(nodesForTokens.get(last), nodesForTokens.get(roleToken), srlArcType);
					setSharedSRLArcInfos(srlToken, arc, role);
					String[] roleDescription;
					if ((roleDescription = srlToken.getRoleDescriptions(role)) != null) {

						arc.setAttributeValue(PB_ROLE_ATTRIBUTE, roleDescription[0]);
						arc.setAttributeValue(VN_ROLE_ATTRIBUTE, roleDescription[1]);
						arc.setAttributeValue(FN_ROLE_ATTRIBUTE, roleDescription[2]);

					}
					last = roleToken;
				}
			}
		}
	}

	private void setSharedSRLArcInfos(SRLToken srlToken, IArc arc, String role) {
		arc.setAttributeValue(ROLE_ATTRIBUTE, role);
		arc.setAttributeValue(CORRESPONDING_VERB_ATTRIBUTE, srlToken.getCorrespondingVerb());
		arc.setAttributeValue(ROLE_CONFIDENCE_ATTRIBUTE, srlToken.getRoleConfidence());
		arc.setAttributeValue(PROP_BANK_ROLESET_ID_ATTRIBUTE, srlToken.getPbRolesetID());
		arc.setAttributeValue(PROP_BANK_ROLESET_DESCR_ATTRIBUTE, srlToken.getPbRolesetDescr());
		arc.setAttributeValue(VERB_NET_FRAMES_ATTRIBUTE, Arrays.toString(srlToken.getVnFrames()));
		arc.setAttributeValue(FRAME_NET_FRAMES_ATTRIBUTE, Arrays.toString(srlToken.getFnFrames()));
		arc.setAttributeValue(EVENT_TYPES_ATTRIBUTE, Arrays.toString(srlToken.getEventTypes()));
	}

	@Override
	public String getID() {
		return ID;
	}

}
