package edu.kit.ipd.parse.graphBuilder;

import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.multiasr.MultiASRPipelineStage;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.shallownlp.ShallowNLP;

public class PipelineTest {

	private static GraphBuilder gb;
	private static ShallowNLP snlp;
	private static MultiASRPipelineStage masr;

	PrePipelineData ppd;

	@BeforeClass
	public static void setUp() {
		gb = new GraphBuilder();
		gb.init();
		snlp = new ShallowNLP();
		snlp.init();
		masr = new MultiASRPipelineStage();
		masr.init();
	}

	@Test
	public void fullPipelineTest() {
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
