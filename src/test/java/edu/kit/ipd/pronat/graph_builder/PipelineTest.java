package edu.kit.ipd.pronat.graph_builder;

import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.kit.ipd.pronat.multiasr.MultiASRPipelineStage;
import edu.kit.ipd.pronat.ner.NERTagger;
import edu.kit.ipd.pronat.prepipedatamodel.PrePipelineData;
import edu.kit.ipd.pronat.prepipedatamodel.tools.StringToHypothesis;
import edu.kit.ipd.pronat.shallow_nlp.ShallowNLP;
import edu.kit.ipd.pronat.srl.SRLabeler;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;

public class PipelineTest {

	private static GraphBuilder gb;
	private static ShallowNLP snlp;
	private static MultiASRPipelineStage masr;
	private static SRLabeler srl;
	private static NERTagger ner;

	PrePipelineData ppd;

	@BeforeClass
	public static void setUp() {
		gb = new GraphBuilder();
		gb.init();
		snlp = new ShallowNLP();
		snlp.init();
		srl = new SRLabeler();
		srl.init();
		ner = new NERTagger();
		ner.init();
	}

	@Ignore
	@Test
	public void fullPipelineTest() {
		masr = new MultiASRPipelineStage();
		masr.init();
		ppd = new PrePipelineData();
		Path inputPath = null;
		try {
			inputPath = Paths.get(PipelineTest.class.getClassLoader().getResource("testaudio.flac").toURI());
		} catch (final URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(inputPath);
		ppd.setInputFilePath(inputPath);
		try {
			masr.exec(ppd);
			snlp.exec(ppd);
			ner.exec(ppd);
			srl.exec(ppd);
			gb.exec(ppd);
		} catch (final PipelineStageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(ppd.getGraph().showGraph());
		} catch (final MissingDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testWithTranscription() {
		ppd = new PrePipelineData();
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis("Go to the fridge open its door and grab the orange juice"));
		try {
			snlp.exec(ppd);
			ner.exec(ppd);
			srl.exec(ppd);
			gb.exec(ppd);
		} catch (final PipelineStageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(ppd.getGraph().showGraph());
		} catch (final MissingDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
