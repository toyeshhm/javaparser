/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2026 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.printer.lexicalpreservation;

import static com.github.javaparser.utils.TestUtils.assertEqualsStringIgnoringEol;

import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.TypePatternExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Issue4830Test extends AbstractLexicalPreservingTest {

    @BeforeEach
    void setLanguageLevel() {
        parser.getParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_21);
    }

    @Test
    void removeTypePattern() {
        considerCode("public class PatternMatching {\n"
                + "    public void processObject(Object obj) {\n"
                + "        if (obj instanceof String s) {\n"
                + "            System.out.println(obj);\n"
                + "        }\n"
                + "    }\n"
                + "}");

        cu.findAll(InstanceOfExpr.class).forEach(InstanceOfExpr::removePattern);

        String expected = "public class PatternMatching {\n"
                + "    public void processObject(Object obj) {\n"
                + "        if (obj instanceof String) {\n"
                + "            System.out.println(obj);\n"
                + "        }\n"
                + "    }\n"
                + "}";
        assertEqualsStringIgnoringEol(expected, LexicalPreservingPrinter.print(cu));
    }

    @Test
    void removeRecordPattern() {
        considerExpression("obj instanceof Box(Point(int x, int y), String s)");

        expression.asInstanceOfExpr().removePattern();

        assertEqualsStringIgnoringEol("obj instanceof Box", LexicalPreservingPrinter.print(expression));
    }

    @Test
    void addPattern() {
        considerExpression("obj instanceof String");

        expression.asInstanceOfExpr().setPattern(typePattern("String", "s"));

        assertEqualsStringIgnoringEol("obj instanceof String s", LexicalPreservingPrinter.print(expression));
    }

    @Test
    void replacePattern() {
        considerExpression("obj instanceof String s");

        expression.asInstanceOfExpr().setPattern(typePattern("Integer", "i"));

        assertEqualsStringIgnoringEol("obj instanceof Integer i", LexicalPreservingPrinter.print(expression));
    }

    @Test
    void changePatternTypeAndName() {
        considerExpression("obj instanceof String s");

        TypePatternExpr pattern =
                expression.asInstanceOfExpr().getPattern().get().asTypePatternExpr();
        pattern.setType(new ClassOrInterfaceType(null, "Integer"));
        pattern.setName("i");

        assertEqualsStringIgnoringEol("obj instanceof Integer i", LexicalPreservingPrinter.print(expression));
    }

    private static TypePatternExpr typePattern(String type, String name) {
        return new TypePatternExpr(new NodeList<>(), new ClassOrInterfaceType(null, type), new SimpleName(name));
    }
}
