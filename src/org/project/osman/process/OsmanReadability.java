package org.project.osman.process;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import edu.stanford.nlp.ling.HasWord;

/**
 * Entry class of the OSMAN Readability package for computing Readability Metrics for Arabic text.
 *
 * @author Mahmoud El-Haj
 * dr.melhaj@gmail.com
 * @version 1.0
 */
public class OsmanReadability {

  	protected static PrintWriter writer;

  	/**
  	 * loadData loads the Arabic dictionaries needed by Stanford tekonizer
  	 * and sentence splitter.
  	 */
	@SuppressWarnings("unused")
	public void loadData(){

		ArabicWordSegmenters.loadLangDictionaries();
		
		 // this is your print stream, store the reference 
		// (this will hide system.err printed by Stanford Tekonizer
	  	PrintStream err = System.err;

	  	// now make all writes to the System.err stream silent 
	  	System.setErr(new PrintStream(new OutputStream() {
	  	    public void write(int b) {
	  	    }
	  	}));
	  	
	  	

	}

	
	/**
	 * Computes OSMAN readability metric for Arabic text (if text has no diacritics please use addTashkeel() before calling the method.
	 * 
	 * @param text
	 * @return OSMAN Score in double
	 */
	public double calculateOsman(String text) {
	
	return 200.791 - (1.015 * wordsPerSentence(text)) - (24.18143403 * (percentComplexWords(text) +syllablesPerWords(text)+faseehPerWords(text) +percentLongWords(text)));
	
	}	

  
	/**
	 * calculate Arabic ARI Readability
	 * @param text
	 * @return ARI Score double
	 */
	public double calculateArabicARI(String text) {
		
		return 4.71 *
				( (double)countChars(text) ) / countWords(text)
			+ 0.5 *
				wordsPerSentence(text)
			- 21.43;	
		
	}	
	
	
	/**
	 * calculate Arabic LIX Readability
	 * @param text
	 * @return LIX Score double
	 */
	public double calculateArabicLIX(String text) {
		
		return wordsPerSentence(text) + (countLongWords(text)*100.0) / countWords(text);
		
	}
		
	/**
	 * calculate Arabic Fog Readability
	 * @param text
	 * @return Fog Score double
	 */
	public double calculateArabicFog(String text)
	{
		return (wordsPerSentence(text) + percentComplexWords(text)) * 0.4;
	}


	/**
	 * calculate Arabic Flesch Readability
	 * @param text
	 * @return Flesch Score double
	 */
	public double calculateArabicFlesch(String text)
	{
		return 206.835 - (1.015 * wordsPerSentence(text)) - (84.6 * syllablesPerWords(text));
	}

	/**
	 * calculate Arabic Flesch Kincaid (grade) Readability 
	 * @param text
	 * @return Arabic Flesch Kincaid Score double
	 */
	public double calculateArabicKincaid(String text)
	{
		return ((0.39 * wordsPerSentence(text)) + (11.8 * syllablesPerWords(text))) - 15.59;
	}
	
	
	/**
	 * Helper method computing
	 * <p>
	 * ( NumberOfSyllables / NumberOfWords )
	 * 
	 * @return the overall ratio of syllables per word computed from the text statistics.
	 */
	protected double syllablesPerWords(String text)
	{
		return ((double) (countSyllables(text)) / countWords(text));
	}
	
	
	/**
	 * Helper method computing
	 * <p>
	 * ( NumberOfFaseeh / NumberOfWords )
	 * 
	 * @return the overall ratio of Faseeh chars per word computed from the text statistics.
	 */
	protected double faseehPerWords(String text)
	{
		return ((double) (countFaseeh(text))) / countWords(text);
	}
	
	
	/**
	 * Helper method computing 
	 * <p>
	 * ( NumberOfComplexWords / NumberOfWords )
	 * 
	 * @return percentage of complex words computed from the text statistics.
	 */
	protected double percentComplexWords(String text)
	{
		return (((double) countComplexWords(text)) / countWords(text));
	}
	
	
	/**
	 * Helper method computing 
	 * <p>
	 * ( NumberOfLongWords / NumberOfWords )
	 * 
	 * @return percentage of long words computed from the text statistics.
	 */
	protected double percentLongWords(String text)
	{
		return (((double) countLongWords(text)) / countWords(text));
	}
	
	/**
	 * Helper method computing 
	 * <p>
	 * NumberOfWords / NumberOfSentences
	 * 
	 * @return words per sentence computed from the text statistics.
	 */
	double wordsPerSentence(String text)
	{
		if((double)countSentences(text)==0){
		
		return ((double) countWords(text)) /1;
		}else{
			
			return ((double) countWords(text)) / countSentences(text);
			}
		}
	

	/**
	 * removes diacritics from Arabic text
	 * @param text
	 * @return text without any diacritics
	 */
	protected String removeTashkeel(String text){
		
		String noTashkeel = text.replace("\u064E","")
				.replace("\u064B","").replace("\u064F","")
				.replace("\u064C","").replace("\u0650","")
				.replace("\u064D","").replace("\u0651","")
				.replace("\u0652","").replace("\u0653","")
				.replace("\u0657","").replace("\u0658","");
		
		return noTashkeel;
	}
	
	/**
	 * count number of syllables in a word
	 * @param text
	 * @return number of syllables as int
	 */
	protected int countSyllables(String text){
	
	//class Syllables counts long, short and stress syllables. Long and stress syllables are treated as doubles thus X 2.
	Syllables syllables = Syllables.countAllSyllables(text);
	int longSyll = syllables.longSyllables;
	int shortSyll = syllables.shortSyllables;
	int stressSyll = syllables.stressSyllables;
	int syllablesCount = (longSyll * 2) + shortSyll + (stressSyll * 2);
		
	return syllablesCount;
		
}

	/**
	 * count number of faseeh indicators in complex words
	 * @param text
	 * @return number of faseeh indicators as int
	 */
	protected int countFaseeh(String text){
		
		int faseeh = 0;
		//we don't need an accurate word segmentation here as non-word object does not contain syllables.
		String[] words = text.split(" ");
		
		for(int i=0;i<words.length;i++){
			
			//class Syllables counts long, short and stress syllables. Long and stress syllables are treated as doubles thus X 2.
			Syllables syllables = Syllables.countAllSyllables(words[i]);
			int longSyll = syllables.longSyllables;
			int shortSyll = syllables.shortSyllables;
			int stressSyll = syllables.stressSyllables;
			int syllablesCount = longSyll * 2 + shortSyll + stressSyll * 2;

	
			if(syllablesCount>4){
			
		int s8 = StringUtils.countMatches(words[i], "\u0626");//� hamza 3ala nabira
		int s9 = StringUtils.countMatches(words[i], "\u0621");//� hamza 3ala satr
		int s10 = StringUtils.countMatches(words[i], "\u0624");//� hamza 3ala waw 
		int s11 = StringUtils.countMatches(words[i], "\u0648\u0627 ");//�� waw wa alef
		int s12 = StringUtils.countMatches(words[i], "\u0648\u0646 ");//�� waw wa noon (jam3 mothakar)
		int s13 = StringUtils.countMatches(words[i], "\u0630");//� Thal (9th letter in Arabic alphabet)
		int s14 = StringUtils.countMatches(words[i], "\u0638");//�  DHaA (17th letter in Arabic alphabet)
		//if a complex word contains at least one faseeh indicator
		if((s8+s9+s10+s11+s12+s13+s14)>=1)
				faseeh++;
	
			}
			
			}
		
		return faseeh;
		
}
	
	/**
	 * count number of characters in a word excluding digits and spaces
	 * @param text
	 * @return number of characters as int
	 */
	protected int countChars(String text){	
		text = removeTashkeel(text);
		return text.replaceAll("\\d"," ").replace(" ","").trim().length();
			
		}
	
	
	/**
	 * count number of words using Stanford Arabic word Segmenter.
	 * @param text
	 * @return number of words as int
	 */
	protected int countWords(String text){	
	List<HasWord> arabicList;
		arabicList = ArabicWordSegmenters.runArabicSegmenter(text);
		int wordCount = arabicList.size();
	
		return wordCount;
		
	}

	/**
	 * count number of complex words in text, those are words with more than 4 syllables.
	 * @param text
	 * @return number of complex words as int
	 */
	protected int countComplexWords(String text){
	int complexWords = 0;
	String[] words = text.split(" ");//we don't need an accurate word segmentation here as non-word object does not contain syllables.
	
	for(int i=0;i<words.length;i++){
		int ss1 = StringUtils.countMatches(words[i], "\u064E");//fatHa
		int ss2 = StringUtils.countMatches(words[i], "\u064B");//tanween fatH
		int ss3 = StringUtils.countMatches(words[i], "\u064F");//dhamma
		int ss4 = StringUtils.countMatches(words[i], "\u064C");//tanween dhamm
		int ss5 = StringUtils.countMatches(words[i], "\u0650");//kasra
		int ss6 = StringUtils.countMatches(words[i], "\u064D");//tanween kasr
		int ss7 = StringUtils.countMatches(words[i], "\u0651");//shaddah (this is doubled as it's a double sound)
		
		if(ss1+ss2+ss3+ss4+ss5+ss6+ss7 > 4){
			complexWords++;
		}
		
	}
		return complexWords;
	}
	

	/**
	 * count number of long words in text, those are words with more than 5 characters.
	 * @param text
	 * @return number of long words as int
	 */
	protected int countLongWords(String text){	
	List<HasWord> arabicList = null;
	text = removeTashkeel(text);
	arabicList = ArabicWordSegmenters.runArabicSegmenter(text);
	int longWords = 0;
	for (HasWord element : arabicList) {
		if(element.toString().length()>5){
			++longWords;
		}
	}
	return longWords;
	}
		
	
	
	/**
	 * add diacritics (Tashkeel) to Arabic text.
	 * This call Mishkal (which needs to be copied to the project's directory.
	 * @param text
	 * @return diacriticised text as string
	 */
	public  String addTashkeel (String text) throws InterruptedException, IOException {
		
	    String tmpFiles ="tempFiles";
		
	    new File(tmpFiles).mkdirs();
	    
	    File inputFile=File.createTempFile("tmpIn", ".txt");
		String inputF = tmpFiles+File.separator+inputFile.getName();

		writer = new PrintWriter(tmpFiles+File.separator+inputFile.getName(), "UTF-8");
		writer.println(text);
		writer.flush();
		
		File outputFile=File.createTempFile("tmpOut", ".txt");
		String outputF = tmpFiles+File.separator+outputFile.getName();
		Runtime.getRuntime().exec ("cmd /c mishkal-console -f "+inputF+" > "+outputF);

		File outputFILE = new File(outputF);
		 while(!outputFILE.renameTo(outputFILE)) {
		        // Cannot read from file, windows still working on it.
		        Thread.sleep(10);
		    }
		 
		 String content = FileUtils.readFileToString(new File(outputF));
		if(content.indexOf("cache checked word")>-1)
		content =  content.substring(0, content.indexOf("cache checked word")).trim();
		//force encode output to be UTF-8 format  
		String tashkeelUTF8 = new String(content.getBytes("UTF-8"));

		new File(outputF).deleteOnExit();
		new File(inputF).deleteOnExit();
		inputFile.deleteOnExit();
		outputFile.deleteOnExit();
		
return tashkeelUTF8;
	}
	

	//count number of sentences using Stanford tokenizer (Sentence Splitter).
	/**
	 * count number of sentences a) using simpler sentence splitter or b) using Stanford tokenizer (Sentence Splitter) you need to uncomment the code below and include the import statements.
	 * a) might not work if all your text is on one line, I recommend you use Stanford sentence splitter instead or your own delimiters. 
	 * @param text
	 * @return diacriticised text as string
	 */
	 public int countSentences(String text){

/*	      Properties props = new Properties();
	      props.put("annotators", "tokenize, ssplit");
	      StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	      // read some text in the text variable

	      // create an empty Annotation just with the given text
	      Annotation document = new Annotation(text);

	      // run all Annotators on this text
	      pipeline.annotate(document);

	      // these are all the sentences in this document
	      // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	      List<CoreMap> sentences = document.get(SentencesAnnotation.class);  
   	      String lines[] = new String[sentences.size()];
	      */    

		 //This is a sentence splitter that work with paragraph and sentence breaks. You can otherwise use
		 //stanford's sentnece splitter by uncommenting the code above. Note you'll need the following import statements:
		 //import java.util.Properties;
		 //import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
		 //import edu.stanford.nlp.pipeline.Annotation;
		 //import edu.stanford.nlp.pipeline.StanfordCoreNLP;
		 //import edu.stanford.nlp.util.CoreMap;
	    		 
		 String lines[] = text.split("\\r?\\n");
	      			
		return lines.length;

}
	 
	 
	
}