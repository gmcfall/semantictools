/*******************************************************************************
 * Copyright 2012 Pearson Education
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.semantictools.util;


public class DurationUtil {
  private static final long millisPerSecond = 1000;
  private static final long millisPerMinute = 60 * millisPerSecond;
  private static final long millisPerHour = 60 * millisPerMinute;
  private static final long millisPerDay = 24 * millisPerHour;
  private static final long millisPerWeek = 7 * millisPerDay;
  private static final long millisPerYear = 52 * millisPerWeek;
  private static final long millisPerMonth = millisPerYear / 12;
  
  public static String durationAsString(long duration) {
    StringBuilder builder = new StringBuilder();
    
    long years = duration / millisPerYear;
    duration -= years * millisPerYear;
    
    long months = duration / millisPerMonth;
    duration -= months*millisPerMonth;
    
    long weeks = duration / millisPerWeek;
    duration -= weeks*millisPerWeek;
    
    long days = duration / millisPerDay;
    duration -= days*millisPerDay;
    
    long hours = duration / millisPerHour;
    duration -= hours*millisPerHour;
    
    long minutes = duration / millisPerMinute;
    duration -= minutes*millisPerMinute;
    
    long seconds = duration / millisPerSecond;
    long millis = duration - seconds*millisPerSecond;
    
    builder.append('P');
    if (years>0) {
      builder.append(years);
      builder.append('Y');
    }
    if (months>0) {
      builder.append(months);
      builder.append('M');
    }
    if (weeks>0) {
      builder.append(weeks);
      builder.append('W');
    }
    if (days>0) {
      builder.append(days);
      builder.append('D');
    }
    if (hours>0 || minutes>0 || seconds>0) {
      builder.append('T');
    }
    if (hours > 0) {
      builder.append(hours);
      builder.append('H');
    }
    if (minutes > 0) {
      builder.append(minutes);
      builder.append('M');
    }
    if (seconds > 0) {
      builder.append(seconds);
      if (millis>0) {
        builder.append('.');
        builder.append(millis);
      }
      builder.append('S');
    }
    
    return builder.toString();
  }
  
  public static long parseDuration(String text) {
    int years = 0;
    int months = 0;
    int weeks = 0;
    int days = 0;
    int hours = 0;
    int minutes = 0;
    int seconds = 0;
    
    if (text.charAt(0) != 'P') {
      throw new DurationParseException("ISO8601 requires a duration to start with 'P'");
    }
    int mark=1;
    boolean time = false;
    for (int i=1; i<text.length(); i++) {
      char c = text.charAt(i);
      if (c == 'T') {
        mark = i+1;
        time = true;
        continue;
      }
      if (Character.isAlphabetic(c)) {
        int digits = parseInt(text, mark, i);
        switch (c) {
        case 'Y' : years = digits; break;
        case 'M' : 
          if (time) {
            minutes = digits;
          } else {
            months = digits;
          }
          break;
          
        case 'W' : weeks = digits; break;
        case 'D' : days = digits; break;
        case 'H' : hours = digits; break;
        case 'S' : seconds = digits; break;
          
        }
        mark = i+1;
      }
      
    }
    long duration =
        years * millisPerYear +
        months * millisPerMonth +
        weeks * millisPerWeek +
        days * millisPerDay +
        hours * millisPerHour +
        minutes * millisPerMinute +
        seconds * millisPerSecond;
    
    return duration;
  }
  
  private static int parseInt(String text, int start, int end) {
    return Integer.parseInt(text.substring(start, end));
  }
  

}
