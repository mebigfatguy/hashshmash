<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="yes"/>
    
    <xsl:param name="title" />
      
    <xsl:template match="*|@*">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="title" priority="1"> 
            <xsl:copy>
                <xsl:value-of select="$title"/>
            </xsl:copy>
    </xsl:template>

</xsl:transform>