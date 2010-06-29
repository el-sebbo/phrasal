package edu.stanford.nlp.mt.decoder.feat;

import java.util.List;
import java.util.ArrayList;

import edu.stanford.nlp.mt.base.FeatureValue;
import edu.stanford.nlp.mt.base.Featurizable;
import edu.stanford.nlp.mt.base.Sequence;
import edu.stanford.nlp.mt.base.IString;
import edu.stanford.nlp.mt.base.ConcreteTranslationOption;
import edu.stanford.nlp.mt.base.DTUFeaturizable;

import edu.stanford.nlp.mt.train.DTUPhraseExtractor;

/**
 * @author Michel Galley
 */
public class DTULinearDistortionFeaturizer extends StatefulFeaturizer<IString, String> {

	public static final String DEBUG_PROPERTY = "DebugDTULinearDistortionFeaturizer";
	public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty(DEBUG_PROPERTY, "false"));
	public static final String FEATURE_NAME = "LinearDistortion";

  public static final float DEFAULT_FUTURE_COST_DELAY = Float.parseFloat(System.getProperty("dtuFutureCostDelay","0f"));

  public final float futureCostDelay;

  @SuppressWarnings("unused")
  public DTULinearDistortionFeaturizer() {
    futureCostDelay = DEFAULT_FUTURE_COST_DELAY;
    System.err.println("Future cost delay: "+futureCostDelay);
  }

  @SuppressWarnings("unused")
  public DTULinearDistortionFeaturizer(String... args) {
    futureCostDelay = Float.parseFloat(args[0]);
    System.err.println("Future cost delay: "+futureCostDelay);
  }

  @Override
	public FeatureValue<String> featurize(Featurizable<IString,String> f) {
    return null;
  }

	@Override
	public List<FeatureValue<String>> listFeaturize(Featurizable<IString,String> f) {

    if (f instanceof DTUFeaturizable)
      if (((DTUFeaturizable)f).targetOnly) {
        f.setState(this, f.prior.getState(this));
        return null;
      }

    ///////////////////////////////////////////
    // (1) Source gaps:
    ///////////////////////////////////////////

    List<FeatureValue<String>> list = new ArrayList<FeatureValue<String>>(1);
    int span = f.option.foreignCoverage.length()-f.option.foreignCoverage.nextSetBit(0);
    int totalSz = 0;
    for(IString fw : f.foreignPhrase)
      if(fw.id != DTUPhraseExtractor.GAP_STR.id)
        ++totalSz;
    int gapSz = span-totalSz;

    ///////////////////////////////////////////
    // (2) Standard linear distortion features:
    ///////////////////////////////////////////

    int linearDistortion = f.linearDistortion + gapSz;
    linearDistortion += LinearFutureCostFeaturizer.getEOSDistortion(f);
    float oldFutureCost = f.prior != null ? ((Float) f.prior.getState(this)) : 0.0f;
    float futureCost;
    if (f.done) {
      futureCost = 0.0f;
    } else {
      futureCost = (1.0f-futureCostDelay)*LinearFutureCostFeaturizer.futureCost(f) + futureCostDelay*oldFutureCost;
      f.setState(this, futureCost);
    }
    float deltaCost = futureCost - oldFutureCost;
    list.add(new FeatureValue<String>(FEATURE_NAME, -1.0*(linearDistortion+deltaCost)));

    return list;
  }

  @Override
	public void initialize(List<ConcreteTranslationOption<IString>> options,
			Sequence<IString> foreign) {
	}

	public void reset() { }
}