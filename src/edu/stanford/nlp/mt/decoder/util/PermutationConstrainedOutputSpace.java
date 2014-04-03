package edu.stanford.nlp.mt.decoder.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.berkeley.nlp.util.CollectionUtils;
import edu.stanford.nlp.mt.base.ConcreteRule;
import edu.stanford.nlp.mt.base.Featurizable;
import edu.stanford.nlp.mt.base.Sequence;
import edu.stanford.nlp.util.StringUtils;

/**
 * Constrains the output space such that the
 * output never deviates more than DEVIATION_LIMIT
 * from a specified permutation sequence.
 * 
 * @author Sebastian Schuster
 */

public class PermutationConstrainedOutputSpace<TK, FV> implements OutputSpace<TK, FV> {

  
  private List<Integer> refPermutationSequence;
  private static final int DEVIATION_LIMIT = 5;
  
  public PermutationConstrainedOutputSpace(List<Integer> permutationSequence) {
    this.refPermutationSequence = permutationSequence;
  }
  
  public PermutationConstrainedOutputSpace() {
  }

  public void setSourceSequence(Sequence<TK> sourceSequence) {}

  public List<ConcreteRule<TK, FV>> filter(List<ConcreteRule<TK, FV>> ruleList) {
    return ruleList;
  }

  
  private List<Integer> getPermutationSequence(Featurizable<TK, FV> featurizable) {
    List<Integer> permutationSequence = new LinkedList<Integer>();
    
    Featurizable<TK, FV> prior = featurizable;
    while (prior != null) {
      int end = prior.sourcePosition + prior.sourcePhrase.size() - 1;
      int start = prior.sourcePosition;
      for (int i = end; i >= start; i--) {
        permutationSequence.add(i);
      }
      prior = prior.prior;
    }
    
    Collections.reverse(permutationSequence);
    if (featurizable != null && featurizable.sourceSentence != null && featurizable.targetPrefix != null) { 
      System.err.println("-----------------------------------");
      System.err.println("Source Phrase:" + featurizable.sourceSentence.toString(" "));
      System.err.println("Permutation Sequence:" + StringUtils.join(permutationSequence));
      System.err.println("Reference Permutation: " + StringUtils.join(this.refPermutationSequence));
      System.err.println("Current target translation: " + featurizable.targetPrefix.toString(" "));
      System.err.println("-----------------------------------");
    }
    return permutationSequence;
  }
  
  
  public boolean allowableContinuation(Featurizable<TK, FV> featurizable,
      ConcreteRule<TK, FV> rule) {
    
    List<Integer> permutationSequence = getPermutationSequence(featurizable);
    int sourceLen = featurizable != null && featurizable.sourcePhrase != null ? featurizable.sourcePhrase.size() : 0;
    int permutationLen = permutationSequence.size();
    int start = permutationLen - sourceLen;
    List<Integer>localPermutation = new ArrayList<Integer>(permutationSequence.subList(start, permutationLen));
    List<Integer>localRefPermutation = new ArrayList<Integer>(this.refPermutationSequence.subList(start, permutationLen));
    CollectionUtils.sort(localPermutation);
    CollectionUtils.sort(localRefPermutation);
    for (int i = 0; i < sourceLen; i++) {
     int a = localPermutation.get(i);
     int b = localPermutation.get(i);
     if (Math.abs(a-b) > DEVIATION_LIMIT) {
       System.err.print("DISALLOWED!");
       return false;
     }
    }
    return true;
  }

  public boolean allowableFinal(Featurizable<TK, FV> featurizable) {
    // TODO Auto-generated method stub
    return true;
  }

  public List<Sequence<TK>> getAllowableSequences() {
    // TODO Auto-generated method stub
    return null;
  }

}