/**
 * 
 */
package com.c4soft.bdp.labs.user.web;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
public record UserResponse(
    String sub,
    String username,
    String email,
    String firstName,
    String lastName) {
}
