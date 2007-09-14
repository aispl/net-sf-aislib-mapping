<?xml version="1.0" encoding="ISO-8859-2" ?>
<!-- 
  -  Anakin, warlock@ais.pl
  -  2001.10.03
  -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml" encoding="iso-8859-2" indent="yes" />

<!-- **************************************************************** -->

<xsl:template match="/">
  <xsl:apply-templates select="node()" />
</xsl:template>

<!-- **************************************************************** -->

<xsl:template match="database">
  <database>
    <xsl:apply-templates select="node()" />
  </database>
</xsl:template>

<!-- **************************************************************** -->

<xsl:template match="table">
  <structure>
    <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
    <sql-table>
      <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
    </sql-table>
    <java-class>
      <xsl:attribute name="name"><xsl:value-of select="@java-name" /></xsl:attribute>
    </java-class>
    <fields>
      <xsl:apply-templates select="column" /> 
    </fields>
  </structure>
</xsl:template>

<!-- **************************************************************** -->

<xsl:template match="column">
  <xsl:variable name="name" select="@name" />
  <xsl:variable name="primary" select="../primary-key[@column-name = $name]/@column-name" />
  <field>
    <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
    <xsl:attribute name="notNull"><xsl:value-of select="@not-null" /></xsl:attribute>
    <xsl:attribute name="primaryKey">
      <xsl:choose>
        <xsl:when test="string-length ($primary) > 0"><xsl:text>true</xsl:text></xsl:when>
        <xsl:otherwise><xsl:text>false</xsl:text></xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
    <sql-field>
      <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
      <xsl:attribute name="type"><xsl:value-of select="@sql-type" /></xsl:attribute>
      <xsl:attribute name="default"><xsl:value-of select="@default-value" /></xsl:attribute>
    </sql-field>
    <java-field>
      <xsl:attribute name="name"><xsl:value-of select="@java-name" /></xsl:attribute>
      <xsl:attribute name="type"><xsl:value-of select="@class-shortcut" /></xsl:attribute>
    </java-field>
  </field>
</xsl:template>

<!-- **************************************************************** -->

</xsl:stylesheet>
