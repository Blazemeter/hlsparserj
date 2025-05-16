package com.comcast.viper.hlsparserj.tags.media;

public class ExtMap extends  Segment {
  @Override
  public String getURI() {
    return tag.getURI();
  }

  /**
   * Returns the length attribute.
   * @return length attribute
   */
  public int getLength() {
    final String line = tag.getAttributes().get("BYTERANGE");
    return Integer.valueOf(line.split("@")[0]);
  }

  /**
   * Returns the offset attribute.
   * @return offset attribute
   */
  public int getOffset() {
    final String line = tag.getAttributes().get("BYTERANGE");
    return Integer.valueOf(line.split("@")[1]);
  }

}
