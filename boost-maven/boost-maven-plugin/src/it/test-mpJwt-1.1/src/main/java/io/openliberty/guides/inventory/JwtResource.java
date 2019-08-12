// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
// tag::jwt[]
package io.openliberty.guides.inventory;

import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import org.eclipse.microprofile.jwt.JsonWebToken;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

@RequestScoped
@Path("jwt")
public class JwtResource {
  // The JWT of the current caller. Since this is a request scoped resource, the
  // JWT will be injected for each JAX-RS request. The injection is performed by
  // the mpJwt-1.0 feature.
  @Inject
  private JsonWebToken jwtPrincipal;

  @GET
  @RolesAllowed({ "admin", "user" })
  @Path("/username")
  public Response getJwtUsername() {
	  System.out.println("Returning JWT User Name");
    return Response.ok(this.jwtPrincipal.getName()).build();
  }

  @GET
  @RolesAllowed({ "admin", "user" })
  @Path("/groups")
  public Response getJwtGroups(@Context SecurityContext securityContext) {
    Set<String> groups = null;
    Principal user = securityContext.getUserPrincipal();
    if (user instanceof JsonWebToken) {
      JsonWebToken jwt = (JsonWebToken) user;
      groups = jwt.getGroups();
    }
    return Response.ok(groups.toString()).build();
  }

  @GET
  @RolesAllowed({ "admin" })
  @Path("/customClaim")
  public Response getCustomClaim(@Context SecurityContext securityContext) {
    if (securityContext.isUserInRole("admin")) {
      String customClaim = jwtPrincipal.getClaim("customClaim");
      return Response.ok(customClaim).build();
    } else {
    	   System.out.println("Error user is not in role admin");
    	   return Response.status(Response.Status.FORBIDDEN).build();
    }
    
  }
  
  
}
// end::jwt[]
