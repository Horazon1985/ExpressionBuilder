package expressionbuilder;

public enum TypeSimplify {

    order_difference_and_division, order_sums_and_products, 
    simplify_trivial, simplify_collect_products, simplify_expand_rational_factors, 
    simplify_expand_short, simplify_expand_moderate, simplify_expand_powerful,
    simplify_expand_and_collect_equivalents_if_shorter,
    simplify_factorize_all_but_rationals, simplify_factorize_all_but_rationals_in_sums, simplify_factorize_all_but_rationals_in_differences, 
    simplify_factorize,
    simplify_reduce_quotients, simplify_reduce_leadings_coefficients, simplify_algebraic_expressions,
    simplify_pull_apart_powers, simplify_multiply_exponents, simplify_functional_relations,
    simplify_replace_exponential_functions_by_definitions, 
    simplify_replace_exponential_functions_with_respect_to_variable_by_definitions, 
    simplify_replace_trigonometrical_functions_by_definitions,
    simplify_replace_trigonometrical_functions_with_respect_to_variable_by_definitions, 
    simplify_expand_products_of_complex_exponential_functions,
    simplify_collect_logarithms, simplify_expand_logarithms;

}
