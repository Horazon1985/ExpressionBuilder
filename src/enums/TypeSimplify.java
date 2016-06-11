package enums;

public enum TypeSimplify {

    // Für alle abstrakten Ausdrücke
    order_sums_and_products,
    simplify_basic, simplify_by_inserting_defined_vars, simplify_collect_products,
    simplify_factorize,
    // Für algebraische Ausdrücke
    order_difference_and_division, simplify_expand_rational_factors,
    simplify_expand_short, simplify_expand_moderate, simplify_expand_powerful,
    simplify_expand_and_collect_equivalents_if_shorter,
    simplify_factorize_all_but_rationals, simplify_bring_expression_to_common_denominator,
    simplify_reduce_quotients, simplify_reduce_differences_and_quotients_advanced, simplify_algebraic_expressions,
    simplify_pull_apart_powers, simplify_multiply_exponents, simplify_functional_relations,
    simplify_replace_exponential_functions_by_definitions,
    simplify_replace_exponential_functions_with_respect_to_variable_by_definitions,
    simplify_replace_trigonometrical_functions_by_definitions,
    simplify_replace_trigonometrical_functions_with_respect_to_variable_by_definitions,
    simplify_expand_products_of_complex_exponential_functions,
    simplify_collect_logarithms, simplify_expand_logarithms,
    // Für Matrizen
    order_difference, simplify_matrix_entries, simplify_factorize_scalars,
    simplify_matrix_functional_relations, simplify_compute_matrix_operations;

}
