package ds.mods.opengx.client.gx.tier2;

public class GXShaderCreator {
	public static final String varyingHeader =
			"varying vec4 position;\n";
	
	public static final String vertexShader = "void main() { position = gl_Position; gl_Position = gl_ModelViewProjectionMatrix*gl_Vertex; }";
	
	public static final String fragmentShader = "void main() {\n"+
			"vec4 prev;\n"+
			"vec4 reg0;\n"+
			"vec4 reg1;\n"+
			"vec4 reg2;\n"+
			"float reg3;\n"+
			"float reg4;\n"+
			"float reg6;\n"+
			"float reg7;\n"+
			"%s\n"+
			"gl_FragColor = prev;\n"+
			"}";
	
	public static final String[] operations = {
		"((($a).$t > ($b).$t) ? ($d) + ($c) : ($d))", //GX_ADD_COMP_GT
		"((($a).$t == ($b).$t) ? ($d) + ($c) : ($d))", //GX_ADD_COMP_EQ
		"((($a).$t > ($b).$t) ? ($d) - ($c) : ($d))", //GX_SUB_COMP_GT
		"((($a).$t == ($b).$t) ? ($d) - ($c) : ($d))", //GX_SUB_COMP_EQ
		"((($a).$t > ($b).$t) ? ($d) * ($c) : ($d))", //GX_MUL_COMP_GT
		"((($a).$t == ($b).$t) ? ($d) * ($c) : ($d))", //GX_MUL_COMP_EQ
		"((($a).$t > ($b).$t) ? ($d) / ($c) : ($d))", //GX_DIV_COMP_GT
		"((($a).$t == ($b).$t) ? ($d) / ($c) : ($d))", //GX_DIV_COMP_EQ
		"$a+$b", //GX_ADD
		"$a-$b", //GX_SUB
		"$a*$b", //GX_MUL
		"$a/$b", //GX_DIV
		"$a", //GX_PASS
		"($a).$t", //GX_ACCESS
		"radians($a)", //GX_CALL_RADIANS
		"degrees($a)", //GX_CALL_DEGREES
		"sine($a)", //GX_CALL_SINE
		"cos($a)", //GX_CALL_COSINE
		"tan($a)", //GX_CALL_TANGENT
		"asin($a)", //GX_CALL_ARCSINE
		"acos($a)", //GX_CALL_ARCCOSINE
		"atan($a)", //GX_CALL_ARCTANGENT
		"pow($a,$b)", //GX_CALL_POWER
		"exp($a)", //GX_CALL_EXP
		"log($a)", //GX_CALL_LOG
		"exp2($a)", //GX_CALL_EXP2
		"log2($a)", //GX_CALL_LOG2
		"sqrt($a)", //GX_CALL_SQRT
		"inversesqrt($a)", //GX_CALL_ISQRT
		"abs($a)", //GX_CALL_ABS
		"sign($a)", //GX_CALL_SIGN
		"floor($a)", //GX_CALL_FLOOR
		"ceil($a)", //GX_CALL_CEIL
		"fract($a)", //GX_CALL_FRACT
		"mod($a,$b)", //GX_CALL_MOD
		"min($a,$b)", //GX_CALL_MIN
		"max($a,$b)", //GX_CALL_MAX
		"clamp($a,$b,$c)", //GX_CALL_CLAMP
		"mix($a,$b,$c)", //GX_CALL_MIX
		"step($a,$b)", //GX_CALL_STEP
		"smoothstep($a,$b,$c)", //GX_CALL_SMOOTHSTEP
		"length($a)", //GX_CALL_LENGTH
		"distance($a,$b)", //GX_CALL_DISTANCE
		"dot($a,$b)", //GX_CALL_DOT
		"cross($a,$b)", //GX_CALL_CROSS
		"normalize($a)", //GX_CALL_NORMALIZE
	};
	
	public static final String[] sources = {
		"prev", //GX_PREV
		"reg0", //GX_REG0
		"reg1", //GX_REG1
		"reg2", //GX_REG2
		"reg3", //GX_REG3
		"reg4", //GX_REG4
		"reg5", //GX_REG5
		"reg6", //GX_REG6
		"reg7", //GX_REG7
		"position" //GX_POSITION
	};
}
