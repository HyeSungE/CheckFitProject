package org.techtown.MyExerciseApp.Data.Exercise;

import java.io.Serializable;
import java.util.ArrayList;

public class CompleteExerciseItem implements Serializable {
   private String compExerciseName;
   private String compExerciseTotalSet;
   private String compExerciseWeight;
   private ArrayList<String> header;
   private ArrayList<String> body;

   public CompleteExerciseItem() {
   }

   public String getCompExerciseName() {
      return compExerciseName;
   }

   public void setCompExerciseName(String compExerciseName) {
      this.compExerciseName = compExerciseName;
   }

   public String getCompExerciseTotalSet() {
      return compExerciseTotalSet;
   }

   public void setCompExerciseTotalSet(String compExerciseTotalSet) {
      this.compExerciseTotalSet = compExerciseTotalSet;
   }

   public String getCompExerciseWeight() {
      return compExerciseWeight;
   }

   public void setCompExerciseWeight(String compExerciseWeight) {
      this.compExerciseWeight = compExerciseWeight;
   }

   public ArrayList<String> getHeader() {
      return header;
   }

   public void setHeader(ArrayList<String> header) {
      this.header = header;
   }

   public ArrayList<String> getBody() {
      return body;
   }

   public void setBody(ArrayList<String> body) {
      this.body = body;
   }
}
