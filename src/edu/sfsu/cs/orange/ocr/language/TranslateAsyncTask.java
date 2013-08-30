/*
 * Copyright 2011 Robert Theis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sfsu.cs.orange.ocr.language;

import edu.sfsu.cs.orange.ocr.CaptureActivity;
import java.net.*;
import java.io.*;

import edu.sfsu.cs.orange.ocr.PersianReshape;

import edu.sfsu.cs.orange.ocr.R;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * Class to perform translations in the background.
 */
public final class TranslateAsyncTask extends AsyncTask<String, String, Boolean> {

  private static final String TAG = TranslateAsyncTask.class.getSimpleName();

  private CaptureActivity activity;
  private TextView textView;
  private View progressView;
  private TextView targetLanguageTextView;
  private String sourceLanguageCode;
  private String targetLanguageCode;
  private String sourceText;
  private String translatedText = "";

  public TranslateAsyncTask(CaptureActivity activity, String sourceLanguageCode, String targetLanguageCode, 
      String sourceText) {
    this.activity = activity;
    this.sourceLanguageCode = sourceLanguageCode;
    this.targetLanguageCode = targetLanguageCode;
    this.sourceText = sourceText;
    textView = (TextView) activity.findViewById(R.id.translation_text_view);
    progressView = (View) activity.findViewById(R.id.indeterminate_progress_indicator_view);
    targetLanguageTextView = (TextView) activity.findViewById(R.id.translation_language_text_view);
  }
  
  @Override
  protected Boolean doInBackground(String... arg0) {
    //translatedText = Translator.translate(activity, sourceLanguageCode, targetLanguageCode, sourceText);

	  try {
		 translatedText = translate("en", "fa", sourceText);
		 translatedText = PersianReshape.reshape(translatedText);
		  
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	/*
	  
    // Check for failed translations.
    if (translatedText.equals(Translator.BAD_TRANSLATION_MSG)) {
      return false;
    }
    */
    return true;
  }

  @Override
  protected synchronized void onPostExecute(Boolean result) {
    super.onPostExecute(result);
    
    if (result) {
      //Log.i(TAG, "SUCCESS");
      if (targetLanguageTextView != null) {
        targetLanguageTextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL);
      }
      textView.setText(translatedText);
      textView.setVisibility(View.VISIBLE);
      textView.setTextColor(activity.getResources().getColor(R.color.translation_text));

      // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
      int scaledSize = Math.max(22, 32 - translatedText.length() / 4);
      textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);

    } else {
      Log.e(TAG, "FAILURE");
      targetLanguageTextView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
      targetLanguageTextView.setText("Unavailable");

    }
    
    // Turn off the indeterminate progress indicator
    if (progressView != null) {
      progressView.setVisibility(View.GONE);
    }
  }
  
  
  public static String translate(String sl, String tl, String text) throws IOException{
      // fetch
      URL url = new URL("http://translate.google.com.tw/translate_a/t?client=t&hl=en&sl=" +
              sl + "&tl=" + tl + "&ie=UTF-8&oe=UTF-8&multires=1&oc=1&otf=2&ssel=0&tsel=0&sc=1&q=" + 
              URLEncoder.encode(text, "UTF-8"));
      URLConnection urlConnection = url.openConnection();
      urlConnection.setRequestProperty("User-Agent", "Something Else");
      BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
      String result = br.readLine();
      br.close();
      // parse
      // System.out.println(result);
      result = result.substring(2, result.indexOf("]]") + 1);
      StringBuilder sb = new StringBuilder();
      String[] splits = result.split("(?<!\\\\)\"");
      for(int i = 1; i < splits.length; i += 8)
          sb.append(splits[i]);
      return sb.toString().replace("\\n", "\n").replaceAll("\\\\(.)", "$1");
  }
}
