package net.sf.aislib.tools.mapping.library;

import org.jdom.Element;

public class XmlDesc {
  String name;
  Element element;
  
  public XmlDesc( String name, Element element ) {
    setName(name);
    setElement( element );
  }

  /**
   * @return
   */
  public Element getElement() {
    return element;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * @param element
   */
  public void setElement(Element element) {
    this.element = element;
  }

  /**
   * @param string
   */
  public void setName(String string) {
    name = string;
  }

}