package com.alibaba.dubbo.common.serialize.support.protostuff;

import com.alibaba.dubbo.common.compiler.support.ClassUtils;
import io.protostuff.*;
import io.protostuff.runtime.RuntimeSchema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 1、基于Protostuff的序列化和反序列化工具。简化版，主要的改进在于，反序列化时，不需要传递对象了。
 * 性能稍差，主要的损失在于反射构造对象。以及一些数组拷贝。<br>
 * 2、另外方法要成对使用toBytes-fromBytes，serialize-deserialize.<br>
 * 3、建议使用对象包装集合和map，这样性能会好。<br>
 * 4、对于List会反序列化成ArrayList，Set会反序列成HashSet<br>
 * 5、对于Map只支持Map&lt;String, Object&gt;，反序列化成HashMap&lt;String, Object&gt;
 * @author yinlei
 * @since 2013-12-12 17:32
 */
public class ProtoUtils {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * 将对象序列化成字节数组
	 * @param object 要被序列化的对象
	 * @param type 对象类型
	 * @return 序列化后的字节数组
	 */
	@SuppressWarnings("unchecked")
	public static byte[] toBytes(Object object, int type) {
		if (object == null) {
			return null;
		}

		byte[] bytes;
		if (type >= 4) {
			bytes = object.toString().getBytes(UTF8);
			int byteLength = bytes.length;
			byte[] destBytes = new byte[byteLength + 1];
			switch (type) {
				case 4:
					destBytes[0] = 4;
					break;
				case 5:
					destBytes[0] = 5;
					break;
				case 6:
					destBytes[0] = 6;
					break;
				case 7:
					destBytes[0] = 7;
					break;
				case 8:
					destBytes[0] = 8;
					break;
				case 9:
					destBytes[0] = 9;
					break;
				case 10:
					destBytes[0] = 10;
					break;
				case 11:
					destBytes[0] = 11;
					break;
				case 12:
					destBytes[0] = 12;
					break;
				case 13:
					destBytes = new byte[2];
					destBytes[0] = 12;
					Boolean bool = (Boolean) object;
					if (bool) {
						destBytes[1] = 1;
					}
					return destBytes;
				default: // 非基本类型
					break;
			}
			System.arraycopy(bytes, 0, destBytes, 1, byteLength);
			return destBytes;
		}

		Class<Object> clazz;
		if (object instanceof List) {
			List<Object> list = (List<Object>) object;
			if (list.isEmpty()) {
				return null;
			}
			clazz = (Class<Object>) list.get(0).getClass();

			bytes = collectToBytes(clazz, list);

			return build(bytes, clazz, 1);
		} else if (object instanceof Set) {
			Set<Object> set = (Set<Object>) object;
			if (set.isEmpty()) {
				return null;
			}
			clazz = (Class<Object>) set.iterator().next().getClass();

			bytes = collectToBytes(clazz, set);

			return build(bytes, clazz, 2);
		} else if (object instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) object;
			if (map.isEmpty()) {
				return null;
			}
			clazz = (Class<Object>) map.values().iterator().next().getClass();
			bytes = mapToBytes(clazz, map);

			return build(bytes, clazz, 3);
		} else {
			clazz = (Class<Object>) object.getClass();
			Schema<Object> schema = RuntimeSchema.getSchema(clazz);
			LinkedBuffer buffer = LinkedBuffer.allocate();
			bytes = ProtobufIOUtil.toByteArray(object, schema, buffer);

			return build(bytes, clazz, 0);
		}
	}

	private static byte[] mapToBytes(Class<Object> clazz, Map<String, Object> map) {
		Schema<Object> schema = RuntimeSchema.getSchema(clazz);
		StringMapSchema<Object> collectionSchema = new StringMapSchema<>(schema);
		LinkedBuffer buffer = LinkedBuffer.allocate(1024);
		return ProtobufIOUtil.toByteArray(map, collectionSchema, buffer);
	}

	private static byte[] collectToBytes(Class<Object> clazz, Collection<Object> list) {
		Schema<Object> schema = RuntimeSchema.getSchema(clazz);
		MessageCollectionSchema<Object> collectionSchema = new MessageCollectionSchema<>(schema);
		LinkedBuffer buffer = LinkedBuffer.allocate(1024);
		return ProtobufIOUtil.toByteArray(list, collectionSchema, buffer);
	}

	public static byte[] build(byte[] bytes, Class<Object> clazz, int type) {
		int byteLength = bytes.length;
		byte[] nameBytes = clazz.getName().getBytes();
		int length = nameBytes.length;
		byte[] destBytes = new byte[byteLength + length + 5];
		destBytes[0] = (byte) type;

		mergeBytes(length, destBytes, 1, 4);
		System.arraycopy(nameBytes, 0, destBytes, 5, length);
		System.arraycopy(bytes, 0, destBytes, length + 5, byteLength);
		return destBytes;
	}

	/**
	 * 将int转为byte数组，字节数组的低位是整型的低字节位
	 * @param source 要转换的int值
	 * @param dests 目标数组
	 * @param from 目标数组起始位置（含）
	 * @param end 目标数组结束位置（含）
	 */
	public static void mergeBytes(int source, byte[] dests, int from, int end) {
		int j = 0;
		for (int i = from; i <= end; i++) {
			dests[i] = (byte) (source >> 8 * j & 0xFF);
			j++;
		}
	}

	/**
	 * 将字节数组反序列化成对象
	 * @param bytes 字节数组
	 * @return 反序列化后的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromBytes(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		int byteLength = bytes.length;
		int type = bytes[0];
		// 处理基本类型，protobuf处理基本类型慢
		switch (type) { // switch比if else快很多
			case 4:
				String args = new String(bytes, 1, byteLength - 1, UTF8);
				Integer i = new Integer(args);
				return (T) i;
			case 5:
				args = new String(bytes, 1, byteLength - 1, UTF8);
				Long l = new Long(args);
				return (T) l;
			case 6:
				args = new String(bytes, 1, byteLength - 1, UTF8);
				Double d = new Double(args);
				return (T) d;
			case 7:
				args = new String(bytes, 1, byteLength - 1, UTF8);
				BigInteger bi = new BigInteger(args);
				return (T) bi;
			case 8:
				args = new String(bytes, 1, byteLength - 1, UTF8);
				BigDecimal bd = new BigDecimal(args);
				return (T) bd;
			case 9:
				args = new String(bytes, 1, byteLength - 1, UTF8);
				Byte b = new Byte(args);
				return (T) b;
			case 10:
				args = new String(bytes, 1, byteLength - 1, UTF8);
				Float f = new Float(args);
				return (T) f;
			case 11:
				args = new String(bytes, 1, byteLength - 1, UTF8);
				Short s = new Short(args);
				return (T) s;
			case 12:
				args = new String(bytes, 1, byteLength - 1, UTF8);
				return (T) args;
			case 13:
				byte bb = bytes[1];
				if (bb == 1) {
					return (T) Boolean.TRUE;
				} else {
					return (T) Boolean.FALSE;
				}
			default:
				break;
		}

		int length = getLength(bytes);
		String className = new String(bytes, 5, length, UTF8);
		Class clazz = ClassUtils.forName(className);
		Schema schema = RuntimeSchema.getSchema(clazz);

		int offset = length + 5;
		int destLength = byteLength - offset;

		switch (type) {
			case 0:
				Object entity = null;
				try {
					entity = clazz.newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				ProtobufIOUtil.mergeFrom(bytes, offset, destLength, entity, schema);
				return (T) entity;
			case 1:
				MessageCollectionSchema<Object> collectionSchema = new MessageCollectionSchema<>(schema);
				List<Object> list = new ArrayList<>();
				ProtobufIOUtil.mergeFrom(bytes, offset, destLength, list, collectionSchema);
				return (T) list;
			case 2:
				collectionSchema = new MessageCollectionSchema<>(schema);
				Set<Object> set = new HashSet<>();
				ProtobufIOUtil.mergeFrom(bytes, offset, destLength, set, collectionSchema);
				return (T) set;
			case 3:
				StringMapSchema<Object> stringSchema = new StringMapSchema<>(schema);
				Map<String, Object> map = new HashMap<>();
				ProtobufIOUtil.mergeFrom(bytes, offset, destLength, map, stringSchema);
				return (T) map;
			default:
				throw new RuntimeException("未知类型protos：" + type);
		}
	}

	private static int getLength(byte[] res) {
		return (res[1] & 0xff) | ((res[2] << 8) & 0xff00) | ((res[3] << 24) >>> 8) | (res[4] << 24);
	}
}
