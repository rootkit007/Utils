package com.greatnowhere.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

public class Reflection {

	/**
	 * Will contain classloader that is to be used by all other classes
	 */
	public static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); 
	
	/**
	 * Reflectively calls specified method on specified class instance with specified parms
	 * Returns whatever the method did, throws some sort of exception if SHTF
	 * @param class instance
	 * @param methodName
	 * @param parms
	 * @return whatever the dang method returns
	 * @throws Throwable
	 */
	public static Object callMethod(Object instance, String methodName, Object... parms) {

		Object _retval = null;
		// Find either a public inherited method
		Method _m = findMethod(instance, methodName, parms);
		// or private/public/protected declared method
		_m = ( _m == null ? findDeclaredMethod(instance, methodName, parms) : _m);
		if ( _m != null ) {
			_retval = callMethod(instance, _m, parms);
		} else {
			throw new RuntimeException("Cannot find method " + methodName + " with required parms on " + instance.toString());
		}
		
		return _retval;

	}

	/**
	 * Reflectively calls specified method on specified class instance with specified parms
	 * Returns whatever the method did, throws some sort of exception if SHTF
	 * @param class instance
	 * @param methodName
	 * @param parms
	 * @return whatever the dang method returns
	 * @throws Throwable
	 */
	public static Object callMethod(Object instance, Method method, Object... parms) {

		Object _retval = null;
		
		try {
			method.setAccessible(true);
		} catch (SecurityException s) {
			LogUtils.warn(instance, "Cant suppress security checks when calling " + method.getName(), s);
		}
		try {
			_retval = method.invoke(instance, parms);
		} catch (Throwable t) {
			LogUtils.error(instance, "Error calling " + method.getName(), t.getCause());
		}
		
		return _retval;

	}
	
	/**
	 * Finds the first method that will accept specified parameters
	 * Takes polymorphism into account
	 * @param instance
	 * @param methodName
	 * @param parms
	 * @return Method instance or null if not found
	 */
	public static Method findMethod(Object instance, String methodName, Object... parms) {
		
		Method[] _meths = ( instance instanceof Class<?> ? ((Class <?>) instance).getMethods() : instance.getClass().getMethods()); 
		return findMethod(methodName, _meths, parms);
		
	}
	
	/**
	 * Finds the first declared method (including private, protected) that will accept specified parameters
	 * Takes polymorphism into account. Will not find inherited methods
	 * @param instance
	 * @param methodName
	 * @param parms
	 * @return Method instance or null if not found
	 */
	public static Method findDeclaredMethod(Object instance, String methodName, Object... parms) {
		
		Method[] _meths = instance.getClass().getDeclaredMethods();
		return findMethod(methodName, _meths, parms);
		
	}
	
	/**
	 * Finds the first method that will accept specified parameters
	 * Takes polymorphism into account
	 * @param instance
	 * @param methodName
	 * @param parms
	 * @return Method instance or null if not found
	 */
	protected static Method findMethod(String methodName, Method[] methods, Object... parms) {
		
		Method _retval = null;
		
		for ( Method _meth : methods ) {
			Class<?>[] _parms = _meth.getParameterTypes();
			if ( _meth.getName().equals(methodName)) {
				boolean _isCastable = true;
				for ( int i=0; i < _parms.length && i < parms.length; i++ ) {
					Class<?> _clz = _parms[i];
					_isCastable &= ( _clz.isAssignableFrom(parms[i].getClass())); 
				}
				if ( _isCastable && _parms.length == parms.length ) {
					_retval = _meth;
					break;
				} 
			}
		}
		return _retval;
		
	}

	
	/**
	 * Returns array of method names on specified object with specified signature
	 * @param Object
	 * @param method signature
	 * @return Array of method names
	 */
	public static Collection<String> enumerateMethods(Object instance, Class<?>... parms) {

		ArrayList<String> _retval = new ArrayList<String>();
		Method[] _methods = instance.getClass().getDeclaredMethods();
		
		for ( int i=0; i<_methods.length; i++) {
			try {
				Method _m = instance.getClass().getDeclaredMethod(_methods[i].getName(), parms);
				_retval.add(_m.getName());
			} catch ( NoSuchMethodException e) {
				
			}
		}
		
		return _retval;
		
	}

	/**
	 * Returns list of specified class elements (fields) that have specified annotation on them,
	 * and the annotation has a field with specified value
	 * @param Class to inspect
	 * @param Required annotation
	 * @param Annotation's field. Can be null, annotation's fields will not be inspected then
	 * @param Required value of the annotation's field. 
	 * @return Array of Strings containing field names
	 */
	public static String[] getAnnotatedFields(Class<?> cls, Class<? extends Annotation> annotationClass, String annotationField, Object annotationFieldValue) {
		ArrayList<String> _retval = new ArrayList<String>();
		// inspect all fields
		Field[] _flds;
		synchronized (cls) {
			_flds = cls.getFields();
		}
		for ( int i=0; i<_flds.length; i++ ) {
	
			Object _annFieldValue = Reflection.getFieldAnnotationValue(cls, _flds[i].getName(), annotationClass, annotationField);
			
			if ( _flds[i].getAnnotation(annotationClass) != null && 
					Utils.compareObjects(annotationFieldValue, _annFieldValue) == 0 ) {
				_retval.add(_flds[i].getName());
			}
					
		} // for
		return _retval.toArray(new String[] {});
	}

	/**
	 * Returns array of Fields that extend specified interface or class
	 * @param Class who's fields are to be examined
	 * @param extendingClass
	 * @return array of Fields that extend specified interface or class
	 */
	public static Field[] getFieldsImplementing(Class<?> cls, Class<?> extendingClass) {
		ArrayList<Field> _retval = new ArrayList<Field>();
		// inspect all fields
		Field[] _flds = cls.getFields();
		for ( Field field : _flds ) {
			Class<?> _cls = field.getType();
			if ( extendingClass.isAssignableFrom(_cls) ) {
				_retval.add(field);
			}
		}
		return _retval.toArray(new Field[] {});
	}

	/**
	 * Returns array of Strings containing field names that extend specified interface or class
	 * @param Class who's fields are to be examined
	 * @param extendingClass
	 * @return array of Strings containing field names that extend specified interface or class
	 */
	public static String[] getFieldNamesImplementing(Class<?> cls, Class<?> extendingClass) {
		Field[] _fields = getFieldsImplementing(cls, extendingClass);
		String[] _retval = new String[_fields.length];
		for ( int i=0; i<_fields.length; i++ ) {
			_retval[i] = _fields[i].getName();
		}
		return _retval;
	}
	
	/**
	 * Returns field's annotation object if it exists, or null if not
	 * @param cls
	 * @param field
	 * @param annotationClass
	 * @return
	 */
	public static Annotation getFieldAnnotation(Class<?> cls, String field, Class<? extends Annotation> annotationClass) {
		Field _fld;
		Annotation _retval = null;
		try {
			_fld = cls.getField(field);
			_retval = _fld.getAnnotation(annotationClass);
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		}
		return _retval;
	}
	
	/**
	 * Returns list of specified class methods that have specified annotation on them,
	 * and the annotation has a field with specified value
	 * @param Class to inspect
	 * @param Required annotation
	 * @param Annotation's field. Can be null, annotation's fields will not be inspected then
	 * @param Required value of the annotation's field. 
	 * @return Array of Strings containing field names
	 */
	public static String[] getAnnotatedMethods(Class<?> cls, Class<? extends Annotation> annotationClass,
			String annotationField, Object annotationFieldValue, Class<?>... methodParms) {
		ArrayList<String> _retval = new ArrayList<String>();

		Method[] _methods;
		synchronized (cls) {
			_methods = cls.getMethods();
		}
		
		for ( int i=0; i<_methods.length; i++) {
		
			Object _annFieldValue = Reflection.getMethodAnnotationValue(cls, _methods[i].getName(),
					annotationClass, annotationField, methodParms);
			
			if ( _methods[i].getAnnotation(annotationClass) != null && 
					Utils.compareObjects(annotationFieldValue, _annFieldValue) == 0 ) {
				_retval.add(_methods[i].getName());
			}
					
		} // for
		return _retval.toArray(new String[] {});
	}

	/**
	 * Retrieves annotation's value for specified class property
	 * @param Class to inspect
	 * @param Property name on the class
	 * @param Annotation to look up on the property
	 * @param Annotation's field value to return
	 * @return Annotation's field value, or NULL of class/annotation/method not found
	 */
	public static Object getFieldAnnotationValue(Class<?> cls, String propertyId, Class<? extends Annotation> annotation, String annField) {
		
		Object _retval = null;
		
		try {
			Field _fld;
			_fld = cls.getField(propertyId);
			Annotation _ann = _fld.getAnnotation(annotation);
			Class<? extends Annotation> _annType = _ann.annotationType(); 
			Method _annMethod = _annType.getDeclaredMethod(annField);
			_retval  =_annMethod.invoke(_ann);
		} catch (Throwable e) {
		}
		
		return _retval;
	}

	/**
	 * Retrieves annotation's value for specified class property
	 * @param Class to inspect
	 * @param Property name on the class
	 * @param Annotation to look up on the property
	 * @param Annotation's field value to return
	 * @return Annotation's field value, or NULL of class/annotation/method not found
	 */
	public static Object getMethodAnnotationValue(Class<?> cls, String methodName, 
			Class<? extends Annotation> annotation, String annField, Class<?>... methodParms) {
		
		Object _retval = null;
		
		try {
			Method _method;
			_method = cls.getMethod(methodName, methodParms);
			Annotation _ann = _method.getAnnotation(annotation);
			Class<? extends Annotation> _annType = _ann.annotationType(); 
			Method _annMethod = _annType.getMethod(annField);
			_retval  =_annMethod.invoke(_ann);
		} catch (Throwable e) {
		}
		
		return _retval;
	}

	/**
	 * Returns annotation
	 * @param Class
	 * @param Annotation
	 * @param Annotations's field name
	 * @param Default value to return if annotation does not exist on specified class 
	 * @return annotations field value
	 * 
	 */
	public static Object getClassAnnotationValue(Class<?> cls, 
			Class<? extends Annotation> annotation, String annField, Object defaultValue) {
		
		Object _retval = defaultValue;
		
		try {
			Method _annMethod;
			Annotation _ann;
			synchronized (cls) {
				_ann = cls.getAnnotation(annotation);
				Class<? extends Annotation> _annType = _ann.annotationType(); 
				_annMethod = _annType.getMethod(annField);
			}
			_retval  =_annMethod.invoke(_ann);
		} catch (Throwable e) {
		}
		
		return _retval;
	}
	
	
	/**
	 * Returns specified field values of object as array of objects
	 * @param target
	 * @param field names
	 * @return Array of object containing field values
	 */
	public static Object[] getFieldValues(Object target, String[] fieldNames) {
		
		Object[] _retval = ArrayUtils.clone(fieldNames);
		
		for ( int i=0; i<fieldNames.length; i++) {
			_retval[i] = getFieldValue(target, fieldNames[i]);
		}
		
		return _retval;
		
	}

	/**
	 * Returns specified field value of an object 
	 * @param target
	 * @param field name
	 * @return Field value
	 */
	public static Object getFieldValue(Object target, String fieldName) {
	
		Object _retval;
		
		try {
			Field _fld = target.getClass().getField(fieldName);
			_retval = _fld.get(target);
		} catch (Throwable t) {
			try {
				Method _method = target.getClass().getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
				_retval = _method.invoke(target);
			} catch (Throwable t1) {
				_retval = null;
			}
		}
		
		return _retval;
		
	}

	public static void setFieldValue(Object target, String fieldName, Object newVal) {
		
		try {
			Field _fld = target.getClass().getField(fieldName);
			_fld.set(target, newVal);
		} catch (Throwable t) {
			try {
				Method _method = target.getClass().getMethod("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), newVal.getClass());
				_method.invoke(target,newVal);
			} catch (Throwable t1) {
				
			}
		}
		
	}
	
	/**
	 * Returns field value of an object. Works also on protected fields
	 * @param obj
	 * @param fieldName
	 * @return field value, or null if fld does not exist
	 */
	public static Object getDeclaredFieldValue(Object obj, String fieldName) {
		Object _retval = null;
		try {
			Field _fld = obj.getClass().getDeclaredField(fieldName);
			_fld.setAccessible(true);
			_retval = _fld.get(obj);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return _retval;
	}
	
	/**
	 * Returns array of objects containing method return values for specified list of parameter values
	 * @param target
	 * @param parms values
	 * @param Method to call on each of the parms, must accept single String and return Object 
	 * @return Array of object containing method return values
	 */
	public static Object[] getMethodValues(Object target, String[] parms, String methodName) {
		
		ArrayList<Object> _retval = new ArrayList<Object>();
		
		for ( int i=0; i<parms.length; i++) {
			try {
				Method _meth = findMethod(target, methodName, parms[i]);
				_retval.add(_meth.invoke(target, parms[i]));
			} catch (Throwable t) {
				_retval.add(null);
			}
		}
		
		return _retval.toArray();
		
	}
	
	 /**
	   * Get the underlying class for a type, or null if the type is a variable type.
	   * @param type the type
	   * @return the underlying class
	   */
	  public static Class<?> getClass(Type type) {
	    if (type instanceof Class) {
	      return (Class<?>) type;
	    }
	    else if (type instanceof ParameterizedType) {
	      return getClass(((ParameterizedType) type).getRawType());
	    }
	    else if (type instanceof GenericArrayType) {
	      Type componentType = ((GenericArrayType) type).getGenericComponentType();
	      Class<?> componentClass = getClass(componentType);
	      if (componentClass != null ) {
	        return Array.newInstance(componentClass, 0).getClass();
	      }
	      else {
	        return null;
	      }
	    }
	    else {
	      return null;
	    }
	  }	

	  /**
	   * Get the actual type arguments a child class has used to extend a generic base class.
	   *
	   * @param baseClass the base class
	   * @param childClass the child class
	   * @return a list of the raw classes for the actual type arguments.
	   */
	  public static <T> List<Class<?>> getTypeArguments(
	    Class<T> baseClass, Class<?> childClass) {
	    Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
	    Type type = childClass;
	    // start walking up the inheritance hierarchy until we hit baseClass
	    while (! getClass(type).equals(baseClass)) {
	      if (type instanceof Class) {
	        // there is no useful information for us in raw types, so just keep going.
	        type = ((Class<?>) type).getGenericSuperclass();
	      }
	      else {
	        ParameterizedType parameterizedType = (ParameterizedType) type;
	        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
	  
	        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
	        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
	        for (int i = 0; i < actualTypeArguments.length; i++) {
	          resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
	        }
	  
	        if (!rawType.equals(baseClass)) {
	          type = rawType.getGenericSuperclass();
	        }
	      }
	    }

	    // finally, for each actual type argument provided to baseClass, determine (if possible)
	    // the raw class for that type argument.
	    Type[] actualTypeArguments;
	    if (type instanceof Class) {
	      actualTypeArguments = ((Class<?>) type).getTypeParameters();
	    }
	    else {
	      actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
	    }
	    List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
	    // resolve types by chasing down type variables.
	    for (Type baseType: actualTypeArguments) {
	      while (resolvedTypes.containsKey(baseType)) {
	        baseType = resolvedTypes.get(baseType);
	      }
	      typeArgumentsAsClasses.add(getClass(baseType));
	    }
	    return typeArgumentsAsClasses;
	  }

}
