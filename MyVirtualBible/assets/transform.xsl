<?xml version="1.0" encoding="ISO-8859-1" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.w3.org/TR/REC-html40" version="2.0" >
	<xsl:param name="copyright"  />
	<xsl:param name="copyright2"  />
	<xsl:param name="bodystyle"  />
	<xsl:output method="html"
            encoding="ISO-8859-1"
            indent="no"/>	
	<xsl:template match="/">
		<HTML>
			<HEAD>
				<TITLE>Bible</TITLE>
				<link rel="stylesheet" type="text/css" href="style.css" />
			</HEAD>
			<BODY>
			    <xsl:attribute name="style">
			    	<xsl:value-of select="$bodystyle" />
			    </xsl:attribute>
				<xsl:apply-templates />
			</BODY>
		</HTML>
	</xsl:template>
	<xsl:template match="chapter">
		<xsl:apply-templates />
	</xsl:template>
	<xsl:template match="title">
		<h3><xsl:apply-templates /></h3>
	</xsl:template>
	<xsl:template match="verse">
		<sup><xsl:value-of select="@number"/></sup> <xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="copyright">
    	<p/><p/><p/><p style="font-size:small"><i>copyright <br/><xsl:value-of select="$copyright" /><br/><xsl:value-of select="$copyright2" /></i></p>
	</xsl:template>
	
	<xsl:template match="br | b | i | p">
		<xsl:element name="{local-name()}">
	        <xsl:apply-templates/>
	    </xsl:element>
	</xsl:template>
	<xsl:template match="table | td | tr " />
</xsl:stylesheet>
