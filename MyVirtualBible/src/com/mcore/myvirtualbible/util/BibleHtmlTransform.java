package com.mcore.myvirtualbible.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.mcore.myvirtualbible.model.Highlighter;

public class BibleHtmlTransform {

	private static BibleHtmlTransform instance;
	private DocumentBuilderFactory dbFactory;

	public static BibleHtmlTransform getInstance() {
		if (instance == null) {
			instance = new BibleHtmlTransform();
		}
		return instance;
	}

	public BibleHtmlTransform() {
		dbFactory = DocumentBuilderFactory.newInstance();
	}

	public String convert(String xmldata, Map parameters) {
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document output = dBuilder.newDocument();
			Document input = dBuilder.parse(new ByteArrayInputStream(xmldata
					.getBytes("UTF-16")));
			input.getDocumentElement().normalize();
			Element body = createBody(output, parameters);
			copyData(output, body, input.getFirstChild(), parameters);
			fillHoles(body, null);
			output.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			DOMSource source = new DOMSource(output);
			ByteArrayOutputStream resultStr = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(resultStr);
			transformer.transform(source, result);
			return resultStr.toString();
		} catch (Exception e) {
			return "<h3>error</h3>";
		}
	}
	
	private class ContainerVerseNode {
		Element lastVerseNode;
		Node lastCloneNode;
		int counter;
	}
	
	private boolean fillHoles(Element rootIn, ContainerVerseNode containerVerseNode) {
		containerVerseNode = containerVerseNode != null? containerVerseNode : new ContainerVerseNode();
		boolean result = false;
		for (int i = 0; i < rootIn.getChildNodes().getLength(); i++) {
			Node item = rootIn.getChildNodes().item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE ) {
				if (!result && containerVerseNode.lastVerseNode != null) {
					if (addToNewVerseNode(item, containerVerseNode)) {
						i--;
					}
				}
			} else if (item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName() != null) {
				if (isVerseNode(item)) {
					containerVerseNode.lastVerseNode = (Element)item;
					containerVerseNode.lastCloneNode = null;
					containerVerseNode.counter = 0;
					result = true;
				} else {
					if (item.getNodeName().equals("end_data")) {
						return result;
					}
					result = hasInternalVerseElements((Element)item);
					if (!result && containerVerseNode.lastVerseNode != null) {
						if (addToNewVerseNode(item, containerVerseNode)) {
							i--;
						}
					} else {						
						result = fillHoles((Element)item, containerVerseNode);
					}
				}
			}
		}
		return result;
	}
	
	private boolean hasInternalVerseElements(Element rootIn) {
		for (int i = 0; i < rootIn.getChildNodes().getLength(); i++) {
			Node item = rootIn.getChildNodes().item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName() != null) {
				if (isVerseNode(item)) {
					return true;
				} else {
					if (item.getNodeName().equals("end_data")) {
						return false;
					}
					if (hasInternalVerseElements((Element)item)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isVerseNode(Node node) {
		if (node != null && node.getNodeName().equals("a")) {
			Node attr = node.getAttributes().getNamedItem("id");
			if (attr != null && attr.getNodeValue() != null && attr.getNodeValue().startsWith("verse")) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean addToNewVerseNode(Node item, ContainerVerseNode containerVerseNode) {
		if (item.getNodeType() == Node.TEXT_NODE) {
			String value = item.getNodeValue();
			if (value != null && value.replaceAll("\n", "").replaceAll("\t", "").replaceAll("\r", "").trim().length() == 0) {
				return false;
			}
		}
		boolean result = false;
		if (containerVerseNode.lastCloneNode != null && containerVerseNode.lastCloneNode.getParentNode() == item.getParentNode()) {
			result = true;
		} else {
			containerVerseNode.lastCloneNode = containerVerseNode.lastVerseNode.cloneNode(false);
			
			Node attr = containerVerseNode.lastCloneNode.getAttributes().getNamedItem("id");
			String orgName = attr.getNodeValue();
			String newValue = attr.getNodeValue() + "_" + ++containerVerseNode.counter;
			attr.setNodeValue(newValue);
			attr = containerVerseNode.lastCloneNode.getAttributes().getNamedItem("name");
			attr.setNodeValue(newValue);
			attr = containerVerseNode.lastCloneNode.getAttributes().getNamedItem("onclick");
			attr.setNodeValue(attr.getNodeValue().replaceAll("this", "document.getElementById('"+orgName+"')"));
			
			item.getParentNode().insertBefore(containerVerseNode.lastCloneNode, item);
		}
		item.getParentNode().removeChild(item);
		containerVerseNode.lastCloneNode.appendChild(item.cloneNode(true));
		return result;
	}
	
	protected String nodeToStr(Node node) {
		try {			
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node),
					new StreamResult(buffer));
			return buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Element createBody(Document document, Map parameters) {
		Element html = document.createElement("html");
		html.setAttribute("xmlns", "http://www.w3.org/TR/REC-html40");
		Element head = document.createElement("head");
		html.appendChild(head);
		Element title = document.createElement("title");
		title.appendChild(document.createTextNode("My Bible"));
		head.appendChild(title);
		Element link = document.createElement("link");
		link.setAttribute("href", "style.css");
		link.setAttribute("type", "text/css");
		link.setAttribute("rel", "stylesheet");
		head.appendChild(link);
		Element script = document.createElement("script");
		script.setAttribute("src", "mybible.js");
		head.appendChild(script);

		if (parameters != null) {
			List<Highlighter> highlighters = (List<Highlighter>) parameters
					.get("highlighters");
			if (highlighters != null && highlighters.size() > 0) {
				Element css = document.createElement("style");
				css.setAttribute("type", "text/css");
				String data = "";
				for (Iterator iterator = highlighters.iterator(); iterator
						.hasNext();) {
					Highlighter highlighter = (Highlighter) iterator.next();
					if (highlighter != null) {
						String hexColor = String.format("#%06X",
								(0xFFFFFF & highlighter.getColor()));
						data += "." + highlighter.getHighlightClassName()
								+ " {background-color: " + hexColor + ";} ";
					}
				}
				css.setTextContent(data);
				head.appendChild(css);

			}
		}

		Element body = document.createElement("body");
		String bodystyle = (String) parameters.get("bodystyle");
		if (bodystyle != null) {
			body.setAttribute("style", bodystyle);
		}
		body.setAttribute("onclick", "selectText(event,null)");
		html.appendChild(body);
		document.appendChild(html);
		return body;
	}

	private void copyData(Document document, Element rootOut, Node rootIn,
			Map parameters) {
		Node lastNode = rootOut;
		for (int i = 0; i < rootIn.getChildNodes().getLength(); i++) {
			Node item = rootIn.getChildNodes().item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				Node node = document.adoptNode(item.cloneNode(true));
				lastNode.appendChild(node);
			} else {
				if (item.getNodeName() != null
						&& item.getNodeName().equals("copyright")) {
					lastNode = rootOut;
				}
				if (item.getNodeName() != null
						&& item.getNodeName().equals("verse")) {
					Node attr = item.getAttributes().getNamedItem("number");
					Integer[] verseInformation = attr != null? BibleUtilities.getVerseInformation(attr.getNodeValue()): null;
					if (verseInformation != null) {
						String verseNum = BibleUtilities.normalizeVerseInformation(verseInformation);
						Element verseNode = document.createElement("a");
						String verseStr = "verse" + verseNum;
						verseNode.setAttribute("id", verseStr);
						verseNode.setAttribute("name", verseStr);
						verseNode.setAttribute("onclick", "selectText(event,this)");
						if (parameters != null) {
							Map versemap = (Map) parameters.get("highlighterMap");
							if (versemap != null) {
								Highlighter highlighter = (Highlighter) versemap
										.get(verseStr);
								if (highlighter != null) {
									verseNode.setAttribute("class",
											highlighter.getHighlightClassName());
								}
							}
						}
						rootOut.appendChild(verseNode);
						lastNode = verseNode;
					}
				}

				Node node = transformNode(document, lastNode, item, parameters);

				if (node == null) {
					Node clonedNode;
					// Bug en Android 11
					try {
						clonedNode = item.cloneNode(false);
					} catch (Exception e) {
						clonedNode = document.createElement(item.getNodeName());
					}
					node = document.adoptNode(clonedNode);
				}
				Element element = (Element) node;

				lastNode.appendChild(element);
				copyData(document, element, item, parameters);

			}
		}
	}

	private Node transformNode(Document document, Node parent, Node item,
			Map parameters) {
		Node node = null;
		if (item.getNodeName() != null && item.getNodeName().equals("title")) {
			node = document.createElement("h3");
		}
		if (item.getNodeName() != null && item.getNodeName().equals("verse")) {
			Node attr = item.getAttributes().getNamedItem("number");
			String verseNum = attr != null ? attr.getNodeValue() : "?";
			node = document.createElement("sup");
			node.appendChild(document.createTextNode(verseNum));
		}
		if (item.getNodeName() != null
				&& item.getNodeName().equals("copyright")) {
			parent.appendChild(document.createElement("end_data"));
			parent.appendChild(document.createElement("p"));
			parent.appendChild(document.createElement("p"));
			parent.appendChild(document.createElement("p"));
			Element p = document.createElement("p");
			p.setAttribute("style", "font-size:small");
			Element ni = document.createElement("i");
			ni.appendChild(document.createTextNode("copyright "));
			ni.appendChild(document.createElement("br"));
			Object value = getParameterValue(parameters, "copyright", " ---- ");
			if (value instanceof Node) {
				Node node2 = document.adoptNode((Node) value);
				ni.appendChild(node2);
			} else {
				if (value != null) {
					ni.appendChild(document.createTextNode(value.toString()));
				}
			}
			ni.appendChild(document.createElement("br"));
			p.appendChild(ni);
			node = p;
		}
		return node;
	}

	private Object getParameterValue(Map parameters, String key,
			String defaultValue) {
		String result = (String) parameters.get(key);
		if (result == null) {
			result = defaultValue;
		}
		try {
			if (result != null && result.contains("<") && result.contains(">")) {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document input = dBuilder.parse(new ByteArrayInputStream(result
						.getBytes()));
				return input.getFirstChild();
			}
		} catch (Exception e) {

		}
		return result;
	}
}
