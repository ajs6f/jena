/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.jena.riot.thrift.wire;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;

@SuppressWarnings("all")
public class RDF_Literal implements org.apache.thrift.TBase<RDF_Literal, RDF_Literal._Fields>, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RDF_Literal");

  private static final org.apache.thrift.protocol.TField LEX_FIELD_DESC = new org.apache.thrift.protocol.TField("lex", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField LANGTAG_FIELD_DESC = new org.apache.thrift.protocol.TField("langtag", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField DATATYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("datatype", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField DT_PREFIX_FIELD_DESC = new org.apache.thrift.protocol.TField("dtPrefix", org.apache.thrift.protocol.TType.STRUCT, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new RDF_LiteralStandardSchemeFactory());
    schemes.put(TupleScheme.class, new RDF_LiteralTupleSchemeFactory());
  }

  public String lex; // required
  public String langtag; // optional
  public String datatype; // optional
  public RDF_PrefixName dtPrefix; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    LEX((short)1, "lex"),
    LANGTAG((short)2, "langtag"),
    DATATYPE((short)3, "datatype"),
    DT_PREFIX((short)4, "dtPrefix");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // LEX
          return LEX;
        case 2: // LANGTAG
          return LANGTAG;
        case 3: // DATATYPE
          return DATATYPE;
        case 4: // DT_PREFIX
          return DT_PREFIX;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private _Fields optionals[] = {_Fields.LANGTAG,_Fields.DATATYPE,_Fields.DT_PREFIX};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.LEX, new org.apache.thrift.meta_data.FieldMetaData("lex", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.LANGTAG, new org.apache.thrift.meta_data.FieldMetaData("langtag", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.DATATYPE, new org.apache.thrift.meta_data.FieldMetaData("datatype", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.DT_PREFIX, new org.apache.thrift.meta_data.FieldMetaData("dtPrefix", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, RDF_PrefixName.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RDF_Literal.class, metaDataMap);
  }

  public RDF_Literal() {
  }

  public RDF_Literal(
    String lex)
  {
    this();
    this.lex = lex;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public RDF_Literal(RDF_Literal other) {
    if (other.isSetLex()) {
      this.lex = other.lex;
    }
    if (other.isSetLangtag()) {
      this.langtag = other.langtag;
    }
    if (other.isSetDatatype()) {
      this.datatype = other.datatype;
    }
    if (other.isSetDtPrefix()) {
      this.dtPrefix = new RDF_PrefixName(other.dtPrefix);
    }
  }

  public RDF_Literal deepCopy() {
    return new RDF_Literal(this);
  }

  @Override
  public void clear() {
    this.lex = null;
    this.langtag = null;
    this.datatype = null;
    this.dtPrefix = null;
  }

  public String getLex() {
    return this.lex;
  }

  public RDF_Literal setLex(String lex) {
    this.lex = lex;
    return this;
  }

  public void unsetLex() {
    this.lex = null;
  }

  /** Returns true if field lex is set (has been assigned a value) and false otherwise */
  public boolean isSetLex() {
    return this.lex != null;
  }

  public void setLexIsSet(boolean value) {
    if (!value) {
      this.lex = null;
    }
  }

  public String getLangtag() {
    return this.langtag;
  }

  public RDF_Literal setLangtag(String langtag) {
    this.langtag = langtag;
    return this;
  }

  public void unsetLangtag() {
    this.langtag = null;
  }

  /** Returns true if field langtag is set (has been assigned a value) and false otherwise */
  public boolean isSetLangtag() {
    return this.langtag != null;
  }

  public void setLangtagIsSet(boolean value) {
    if (!value) {
      this.langtag = null;
    }
  }

  public String getDatatype() {
    return this.datatype;
  }

  public RDF_Literal setDatatype(String datatype) {
    this.datatype = datatype;
    return this;
  }

  public void unsetDatatype() {
    this.datatype = null;
  }

  /** Returns true if field datatype is set (has been assigned a value) and false otherwise */
  public boolean isSetDatatype() {
    return this.datatype != null;
  }

  public void setDatatypeIsSet(boolean value) {
    if (!value) {
      this.datatype = null;
    }
  }

  public RDF_PrefixName getDtPrefix() {
    return this.dtPrefix;
  }

  public RDF_Literal setDtPrefix(RDF_PrefixName dtPrefix) {
    this.dtPrefix = dtPrefix;
    return this;
  }

  public void unsetDtPrefix() {
    this.dtPrefix = null;
  }

  /** Returns true if field dtPrefix is set (has been assigned a value) and false otherwise */
  public boolean isSetDtPrefix() {
    return this.dtPrefix != null;
  }

  public void setDtPrefixIsSet(boolean value) {
    if (!value) {
      this.dtPrefix = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case LEX:
      if (value == null) {
        unsetLex();
      } else {
        setLex((String)value);
      }
      break;

    case LANGTAG:
      if (value == null) {
        unsetLangtag();
      } else {
        setLangtag((String)value);
      }
      break;

    case DATATYPE:
      if (value == null) {
        unsetDatatype();
      } else {
        setDatatype((String)value);
      }
      break;

    case DT_PREFIX:
      if (value == null) {
        unsetDtPrefix();
      } else {
        setDtPrefix((RDF_PrefixName)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case LEX:
      return getLex();

    case LANGTAG:
      return getLangtag();

    case DATATYPE:
      return getDatatype();

    case DT_PREFIX:
      return getDtPrefix();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case LEX:
      return isSetLex();
    case LANGTAG:
      return isSetLangtag();
    case DATATYPE:
      return isSetDatatype();
    case DT_PREFIX:
      return isSetDtPrefix();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof RDF_Literal)
      return this.equals((RDF_Literal)that);
    return false;
  }

  public boolean equals(RDF_Literal that) {
    if (that == null)
      return false;

    boolean this_present_lex = true && this.isSetLex();
    boolean that_present_lex = true && that.isSetLex();
    if (this_present_lex || that_present_lex) {
      if (!(this_present_lex && that_present_lex))
        return false;
      if (!this.lex.equals(that.lex))
        return false;
    }

    boolean this_present_langtag = true && this.isSetLangtag();
    boolean that_present_langtag = true && that.isSetLangtag();
    if (this_present_langtag || that_present_langtag) {
      if (!(this_present_langtag && that_present_langtag))
        return false;
      if (!this.langtag.equals(that.langtag))
        return false;
    }

    boolean this_present_datatype = true && this.isSetDatatype();
    boolean that_present_datatype = true && that.isSetDatatype();
    if (this_present_datatype || that_present_datatype) {
      if (!(this_present_datatype && that_present_datatype))
        return false;
      if (!this.datatype.equals(that.datatype))
        return false;
    }

    boolean this_present_dtPrefix = true && this.isSetDtPrefix();
    boolean that_present_dtPrefix = true && that.isSetDtPrefix();
    if (this_present_dtPrefix || that_present_dtPrefix) {
      if (!(this_present_dtPrefix && that_present_dtPrefix))
        return false;
      if (!this.dtPrefix.equals(that.dtPrefix))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(RDF_Literal other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    RDF_Literal typedOther = other;

    lastComparison = Boolean.valueOf(isSetLex()).compareTo(typedOther.isSetLex());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLex()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lex, typedOther.lex);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetLangtag()).compareTo(typedOther.isSetLangtag());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLangtag()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.langtag, typedOther.langtag);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDatatype()).compareTo(typedOther.isSetDatatype());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDatatype()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.datatype, typedOther.datatype);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDtPrefix()).compareTo(typedOther.isSetDtPrefix());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDtPrefix()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.dtPrefix, typedOther.dtPrefix);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("RDF_Literal(");
    boolean first = true;

    sb.append("lex:");
    if (this.lex == null) {
      sb.append("null");
    } else {
      sb.append(this.lex);
    }
    first = false;
    if (isSetLangtag()) {
      if (!first) sb.append(", ");
      sb.append("langtag:");
      if (this.langtag == null) {
        sb.append("null");
      } else {
        sb.append(this.langtag);
      }
      first = false;
    }
    if (isSetDatatype()) {
      if (!first) sb.append(", ");
      sb.append("datatype:");
      if (this.datatype == null) {
        sb.append("null");
      } else {
        sb.append(this.datatype);
      }
      first = false;
    }
    if (isSetDtPrefix()) {
      if (!first) sb.append(", ");
      sb.append("dtPrefix:");
      if (this.dtPrefix == null) {
        sb.append("null");
      } else {
        sb.append(this.dtPrefix);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (lex == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'lex' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (dtPrefix != null) {
      dtPrefix.validate();
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class RDF_LiteralStandardSchemeFactory implements SchemeFactory {
    public RDF_LiteralStandardScheme getScheme() {
      return new RDF_LiteralStandardScheme();
    }
  }

  private static class RDF_LiteralStandardScheme extends StandardScheme<RDF_Literal> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, RDF_Literal struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // LEX
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.lex = iprot.readString();
              struct.setLexIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // LANGTAG
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.langtag = iprot.readString();
              struct.setLangtagIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // DATATYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.datatype = iprot.readString();
              struct.setDatatypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // DT_PREFIX
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.dtPrefix = new RDF_PrefixName();
              struct.dtPrefix.read(iprot);
              struct.setDtPrefixIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, RDF_Literal struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.lex != null) {
        oprot.writeFieldBegin(LEX_FIELD_DESC);
        oprot.writeString(struct.lex);
        oprot.writeFieldEnd();
      }
      if (struct.langtag != null) {
        if (struct.isSetLangtag()) {
          oprot.writeFieldBegin(LANGTAG_FIELD_DESC);
          oprot.writeString(struct.langtag);
          oprot.writeFieldEnd();
        }
      }
      if (struct.datatype != null) {
        if (struct.isSetDatatype()) {
          oprot.writeFieldBegin(DATATYPE_FIELD_DESC);
          oprot.writeString(struct.datatype);
          oprot.writeFieldEnd();
        }
      }
      if (struct.dtPrefix != null) {
        if (struct.isSetDtPrefix()) {
          oprot.writeFieldBegin(DT_PREFIX_FIELD_DESC);
          struct.dtPrefix.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class RDF_LiteralTupleSchemeFactory implements SchemeFactory {
    public RDF_LiteralTupleScheme getScheme() {
      return new RDF_LiteralTupleScheme();
    }
  }

  private static class RDF_LiteralTupleScheme extends TupleScheme<RDF_Literal> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, RDF_Literal struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.lex);
      BitSet optionals = new BitSet();
      if (struct.isSetLangtag()) {
        optionals.set(0);
      }
      if (struct.isSetDatatype()) {
        optionals.set(1);
      }
      if (struct.isSetDtPrefix()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetLangtag()) {
        oprot.writeString(struct.langtag);
      }
      if (struct.isSetDatatype()) {
        oprot.writeString(struct.datatype);
      }
      if (struct.isSetDtPrefix()) {
        struct.dtPrefix.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, RDF_Literal struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.lex = iprot.readString();
      struct.setLexIsSet(true);
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.langtag = iprot.readString();
        struct.setLangtagIsSet(true);
      }
      if (incoming.get(1)) {
        struct.datatype = iprot.readString();
        struct.setDatatypeIsSet(true);
      }
      if (incoming.get(2)) {
        struct.dtPrefix = new RDF_PrefixName();
        struct.dtPrefix.read(iprot);
        struct.setDtPrefixIsSet(true);
      }
    }
  }

}

