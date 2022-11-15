package com.redhat.cloudnative;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;

@Path("/api/orders")
public class OrderResource {

    @GET
    public List<Order> list() {
        return Order.listAll();
    }

    @POST
    public List<Order> add(Order order) {
        order.persist();
        return list();
    }

    @GET
    @Path("/{orderId}/{status}")
    public Order updateStatus(String orderId, String status) {
        Order newOrder = Order.findByOrderId(orderId);
        newOrder.status = status;
        newOrder.update();
        return newOrder;

    }

}