<xsl:transform version="1.0" 
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
               xmlns:xalan="http://xml.apache.org/xslt"
               xmlns:hs="xalan://com.mebigfatguy.hashshmash.XSLTBean"
               exclude-result-prefixes="xsl xalan"
               extension-element-prefixes="hs">

    <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="yes"/>
    <xsl:output indent="yes" method="xml" xalan:indent-amount="4"/>
    
    <xsl:param name="title" />
    <xsl:param name="bean" />
      
    <xsl:template match="*|@*">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="title"> 
        <xsl:copy>
            <xsl:value-of select="$title"/>
        </xsl:copy>
    </xsl:template>
 
    <xsl:template match="ul/li/div">
        <xsl:for-each select="hs:getTypes($bean)">
            <xsl:variable name="type" select="."/>
            <div>
		        <h1>Type: <xsl:value-of select="$type"/></h1>
	        </div>
        </xsl:for-each>
    </xsl:template> 
    
</xsl:transform>