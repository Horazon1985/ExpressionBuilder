package expressionbuilder;

public enum TypeSimplify {

    order_difference_and_division, order_sums_and_products, 
    simplify_trivial, collect_products, expand_rational_factors, 
    expand_short, expand_moderate, expand_powerful,
    simplify_expand_and_collect_equivalents_if_shorter,
    factorize_all_but_rationals_in_sums, factorize_all_but_rationals_in_differences, 
    factorize_in_sums, factorize_in_differences, reduce_quotients, reduce_leadings_coefficients, simplify_algebraic_expressions,
    simplify_powers, multiply_powers, simplify_functional_relations,
    simplify_replace_exponential_functions_by_definitions, 
    simplify_replace_exponential_functions_with_respect_to_variable_by_definitions, 
    simplify_replace_trigonometrical_functions_by_definitions,
    simplify_expand_products_of_complex_exponential_functions,
    simplify_collect_logarithms, simplify_expand_logarithms;

}
