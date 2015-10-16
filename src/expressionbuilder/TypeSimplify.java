package expressionbuilder;

public enum TypeSimplify {

    order_difference_and_division, order_sums_and_products, 
    simplify_trivial, collect_products, expand_rational_factors, expand,
    simplify_expand_and_collect_equivalents_if_shorter,
    factorize_all_but_rationals_in_sums, factorize_all_but_rationals_in_differences, 
    factorize_in_sums, factorize_in_differences, reduce_quotients, reduce_leadings_coefficients, simplify_algebraic_expressions,
    simplify_powers, multiply_powers, simplify_functional_relations,
    simplify_replace_exponential_functions_by_definitions, 
    simplify_replace_trigonometrical_functions_by_definitions,
    simplify_expand_powers_and_products_of_trigonometrical_functions,
    simplify_collect_logarithms, simplify_expand_logarithms;

}
