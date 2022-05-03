module test.alipsa.simplerest {
  requires se.alipsa.simplerest;
  requires org.eclipse.jetty.server;
  requires org.eclipse.jetty.servlet;
  requires org.junit.jupiter;
  requires org.junit.platform.engine;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  exports test.alipsa.simplerest;
  exports test.alipsa.simplerest.model;
}