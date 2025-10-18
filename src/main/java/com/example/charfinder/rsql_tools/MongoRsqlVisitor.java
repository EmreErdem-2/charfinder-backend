package com.example.charfinder.rsql_tools;

import cz.jirutka.rsql.parser.ast.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MongoRsqlVisitor implements RSQLVisitor<Criteria, Void> {

    @Override
    public Criteria visit(AndNode node, Void param) {
        List<Criteria> children = node.getChildren().stream()
                .map(child -> child.accept(this, null))
                .toList();
        return new Criteria().andOperator(children.toArray(new Criteria[0]));
    }

    @Override
    public Criteria visit(OrNode node, Void param) {
        List<Criteria> children = node.getChildren().stream()
                .map(child -> child.accept(this, null))
                .toList();
        return new Criteria().orOperator(children.toArray(new Criteria[0]));
    }

    @Override
    public Criteria visit(ComparisonNode node, Void param) {
        String field = node.getSelector();
        String op = node.getOperator().getSymbol();
        List<String> args = node.getArguments();

        switch (op) {
            case "==":
                Object eqVal = parseLiteral(args.getFirst());
                return Criteria.where(field).is(eqVal);

            case "!=":
                return Criteria.where(field).ne(parseLiteral(args.getFirst()));

            case "=gt=":
                return Criteria.where(field).gt(parseLiteral(args.getFirst()));

            case "=ge=":
                return Criteria.where(field).gte(parseLiteral(args.getFirst()));

            case "=lt=":
                return Criteria.where(field).lt(parseLiteral(args.getFirst()));

            case "=le=":
                return Criteria.where(field).lte(parseLiteral(args.getFirst()));

            case "=in=":
                return Criteria.where(field).in(args.stream().map(this::parseLiteral).toList());

            case "=out=":
                return Criteria.where(field).nin(args.stream().map(this::parseLiteral).toList());

            default:
                throw new IllegalArgumentException("Unsupported operator: " + op);
        }
    }


    private Object parseLiteral(String raw) {
        // Try number
        try {
            if (raw.contains(".")) return Double.parseDouble(raw);
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {}

        // Try boolean
        if ("true".equalsIgnoreCase(raw)) return true;
        if ("false".equalsIgnoreCase(raw)) return false;

        // TODO: optionally add date parsing here

        // Fallback: string
        return raw;
    }

}