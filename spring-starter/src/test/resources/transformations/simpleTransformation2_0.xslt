<?xml version='1.0'?>
<xsl:stylesheet
    version="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema">
    
    <xsl:output method="xml"/>
   
	<xsl:template match="laptops">
		<laptops>
	        <xsl:perform-sort select="laptop">
		    	<xsl:sort select="@name" order="ascending"/>
			</xsl:perform-sort>
			</laptops>
	</xsl:template>
</xsl:stylesheet>