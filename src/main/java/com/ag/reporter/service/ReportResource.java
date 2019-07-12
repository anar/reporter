package com.ag.reporter.service;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.mapper.JacksonFacade;
import org.dizitart.no2.mapper.MapperFacade;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ag.reporter.server.DatabaseProvider;

/*
 * 	@author Anar Gasimov
 */

@Path("reports")
@Produces(MediaType.APPLICATION_JSON)
public class ReportResource {

	private static final int DEFAULT_SIZE = 10;

	private static final Logger logger = LoggerFactory.getLogger(ReportResource.class);

	private Nitrite db;

	public ReportResource() {

	}

	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok("SUCCESS").build();
	}

	@GET
	@Path("{id:[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}}")
	public Response get(@PathParam("id") String id) {

		try {

			Document document = DatabaseProvider.get().getCollection("reports").find(Filters.eq("id", id))
					.firstOrDefault();

			if (document == null)
				return Response.status(404).build();

			return Response.ok(document).build();

		} catch (Exception e) {
			logger.error("Failed getting document ID: " + id, e);
		} finally {
			if (db != null && !db.isClosed())
				db.close();
		}

		return Response.serverError().build();

	}

	@PUT
	@Path("{id:[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}}")
	public Response changeState(@PathParam("id") String id, JSONObject ticket) {

		try {

			Document document = DatabaseProvider.get().getCollection("reports").find(Filters.eq("id", id))
					.firstOrDefault();

			if (document == null)
				return Response.status(404).build();

			if (ticket == null || ticket.get("ticketState") == null)
				return Response.status(406).build();

			String ticketStatus = ticket.get("ticketState").toString();

			if ("CLOSED".contentEquals(ticketStatus) || "BLOCKED".contentEquals(ticketStatus)) {
				document.replace("state", ticketStatus);
				DatabaseProvider.get().getCollection("reports").update(Filters.eq("id", id), document);
			}

			return Response.ok(document).build();

		} catch (Exception e) {
			logger.error("Failed changing report state: ", e);
		} finally {
			if (db != null && !db.isClosed())
				db.close();
		}

		return Response.serverError().build();

	}

	@GET
	public Response getAll(@QueryParam("size") int size,
			@DefaultValue("") @QueryParam("nextOffset") String nextOffset) {

		if (size < 1)
			size = DEFAULT_SIZE;

		try {

			List<Document> documents = DatabaseProvider.get().getCollection("reports")
					.find(Filters.and(Filters.not(Filters.eq("state", "CLOSED")), Filters.gt("created", nextOffset)),
							FindOptions.sort("created", SortOrder.Ascending).thenLimit(0, size))
					.toList();

			// Quick solution, it should be replaced with generic ResponseWrapper class
			HashMap<String, Object> wrapper = new HashMap<>();
			wrapper.put("size", String.valueOf(documents.size()));
			if (documents.size() > 0) {
				nextOffset = documents.get(documents.size() - 1).get("created").toString();
			}
			wrapper.put("nextOffset", nextOffset);
			wrapper.put("elements", documents);

			return Response.ok(wrapper).build();

		} catch (Exception e) {
			logger.error("Failed getting reports: ", e);
		} finally {
			if (db != null && !db.isClosed())
				db.close();
		}

		return Response.serverError().build();

	}
}