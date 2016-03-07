package com.mcore.myvirtualbible.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import android.content.Context;
import android.content.res.AssetManager;

public class XmlTransform {
	
	public static String transformChapterToHTML(Context ctx, String xml, Map parameters) {
		AssetManager assetManager = ctx.getAssets();
		try {
			InputStream isXls = assetManager.open("transform.xsl");
			return getTransformedHtml(new ByteArrayInputStream(xml.getBytes()), isXls, parameters);
		} catch (Exception e) {
			return "<h3>error</h3>";
		}
	}
	
	public static String getTransformedHtml(String xml, String xsl, Map parameters)
			throws TransformerException {
		return getTransformedHtml(xml.getBytes(), xsl.getBytes(), parameters);
	}

	public static String getTransformedHtml(byte[] xml, byte[] xsl, Map parameters)
			throws TransformerException {
		return getTransformedHtml(new ByteArrayInputStream(xml), new ByteArrayInputStream(xsl), parameters);
	}
	
	public static String getTransformedHtml(InputStream xml, InputStream xsl, Map parameters)
			throws TransformerException {
		Source srcXml = new StreamSource(xml);
		Source srcXsl = new StreamSource(xsl);
		StringWriter writer = new StringWriter();
		Result result = new StreamResult(writer);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(srcXsl);
		if (parameters != null) {
			for (Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();) {
				Object key = (Object) iterator.next();
				Object value = parameters.get(key);
				if (key != null && value != null) {
					transformer.setParameter(key.toString(), value.toString());
				}
			}
		}
		transformer.transform(srcXml, result);
		return writer.toString();
	}

	
}