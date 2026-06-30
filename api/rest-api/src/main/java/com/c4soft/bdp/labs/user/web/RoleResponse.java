/**
 * 
 */
package com.c4soft.bdp.labs.user.web;

import java.util.List;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
public record RoleResponse(String label, List<String> permissionLabels) {

}
