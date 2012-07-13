/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * 
 *  This file is part of DVB Inspector.
 * 
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 * 
 */

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.BitSource;

public class Seq_parameter_set_rbsp extends RBSP {

	
	private static Logger	logger	= Logger.getLogger(Seq_parameter_set_rbsp.class.getName());

	// based on 7.3.2.1.1 Sequence parameter set data syntax Rec. ITU-T H.264 (03/2010) – Prepublished version
	private int profile_idc;
	
	private int constraint_set0_flag;
	private int constraint_set1_flag;
	private int constraint_set2_flag;
	private int constraint_set3_flag;
	private int constraint_set4_flag;
	private int constraint_set5_flag;
	private int reserved_zero_2bits;  // http://www.cardinalpeak.com/blog/?p=878
	private int level_idc;
	private int seq_parameter_set_id;
	private int chroma_format_idc;
	private int separate_colour_plane_flag;
	
	private int bit_depth_luma_minus8;
	private int bit_depth_chroma_minus8 ;
	private int qpprime_y_zero_transform_bypass_flag;
	private int seq_scaling_matrix_present_flag ;
	
	private int [] seq_scaling_list_present_flag=new int [8];
	private int [][] delta_scale = new int [8][];
	private int [] deltas_read = new int[8];  // helper, does not match data in PES  

	private int log2_max_frame_num_minus4;
	private int pic_order_cnt_type;
	private int log2_max_pic_order_cnt_lsb_minus4;

	private int max_num_ref_frames;
	private int gaps_in_frame_num_value_allowed_flag;
	private int pic_width_in_mbs_minus1;
	private int pic_height_in_map_units_minus1;
	private int frame_mbs_only_flag;
	private int mb_adaptive_frame_field_flag;

	private int direct_8x8_inference_flag;
	private int frame_cropping_flag;
	private int frame_crop_left_offset;
	private int frame_crop_right_offset;
	private int frame_crop_top_offset;
	private int frame_crop_bottom_offset;
	private int vui_parameters_present_flag;

	private VuiParameters vui_parameters;

	
	public Seq_parameter_set_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		profile_idc = bitSource.u(8);
		constraint_set0_flag = bitSource.u(1);
		constraint_set1_flag = bitSource.u(1);
		constraint_set2_flag = bitSource.u(1);
		constraint_set3_flag = bitSource.u(1);
		constraint_set4_flag = bitSource.u(1);
		constraint_set5_flag = bitSource.u(1);
		
		reserved_zero_2bits = bitSource.u(2);
		level_idc = bitSource.u(8);
		seq_parameter_set_id = bitSource.ue();
		if( profile_idc == 100 || profile_idc == 110 ||
				profile_idc == 122 || profile_idc == 144 || profile_idc == 44 ||
				profile_idc == 83 || profile_idc == 86 || profile_idc == 118 ||
				profile_idc == 128 ) {
			chroma_format_idc = bitSource.ue();
			if( chroma_format_idc == 3 ){
				separate_colour_plane_flag =bitSource.u(1);
			}
			bit_depth_luma_minus8 =bitSource.ue();
			bit_depth_chroma_minus8 =bitSource.ue();
			qpprime_y_zero_transform_bypass_flag=bitSource.u(1);
			
			seq_scaling_matrix_present_flag =bitSource.u(1);
			if( seq_scaling_matrix_present_flag==1 ){
				for( int i = 0; i < 8; i++ ) { // TODO ? change to "for( i = 0; i < ( ( chroma_format_idc != 3 ) ? 8 : 12 ); i++ )"  as in Rec. ITU-T H.264 (03/2010) 7.3.2.1.1 Sequence parameter set data syntax
					seq_scaling_list_present_flag[ i ] =bitSource.u(1);
					if( seq_scaling_list_present_flag[ i ]!=0 ){
						if( i < 6 ){
							delta_scale[i] = new int[16];
							deltas_read[i] = scaling_list( delta_scale[i], 16,bitSource);
						}else{
							delta_scale[i] = new int[64];
							deltas_read[i] = scaling_list( delta_scale[i], 64,bitSource);
						}
					}
				}
			}
		}
		
		log2_max_frame_num_minus4 = bitSource.ue();
		pic_order_cnt_type = bitSource.ue();
		
		if(pic_order_cnt_type == 0){
			log2_max_pic_order_cnt_lsb_minus4 = bitSource.ue();
		}else if( pic_order_cnt_type == 1 ) {
			logger.warning(" pic_order_cnt_type == 1 not implemented");
//			delta_pic_order_always_zero_flag 0 u(1)
//			offset_for_non_ref_pic 0 se(v)
//			offset_for_top_to_bottom_field 0 se(v)
//			num_ref_frames_in_pic_order_cnt_cycle 0 ue(v)
//			for( i = 0; i < num_ref_frames_in_pic_order_cnt_cycle; i++ )
//			offset_for_ref_frame[ i ] 0 se(v)
		}
		max_num_ref_frames = bitSource.ue();
		gaps_in_frame_num_value_allowed_flag =bitSource.u(1);
		pic_width_in_mbs_minus1 = bitSource.ue();
		pic_height_in_map_units_minus1 = bitSource.ue();
		frame_mbs_only_flag =bitSource.u(1);
		if(frame_mbs_only_flag==0 ){
			mb_adaptive_frame_field_flag=bitSource.u(1);
		}
		direct_8x8_inference_flag=bitSource.u(1);
		frame_cropping_flag=bitSource.u(1);
		if( frame_cropping_flag!=0 ) {
			frame_crop_left_offset = bitSource.ue();
			frame_crop_right_offset = bitSource.ue();
			frame_crop_top_offset = bitSource.ue();
			frame_crop_bottom_offset = bitSource.ue();
		}
		vui_parameters_present_flag=bitSource.u(1);
		if(vui_parameters_present_flag!=0){
			vui_parameters = new VuiParameters(bitSource);
		}
		
		
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("seq_parameter_set_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("profile_idc",profile_idc,getProfileIdcString(profile_idc))));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set0_flag",constraint_set0_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set1_flag",constraint_set1_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set2_flag",constraint_set2_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set3_flag",constraint_set3_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set4_flag",constraint_set4_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set5_flag",constraint_set5_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_2bits",reserved_zero_2bits,null)));
		t.add(new DefaultMutableTreeNode(new KVP("level_idc",level_idc,null)));
		t.add(new DefaultMutableTreeNode(new KVP("seq_parameter_set_id",seq_parameter_set_id,null)));
		if( profile_idc == 100 || profile_idc == 110 ||
				profile_idc == 122 || profile_idc == 244 || profile_idc == 44 ||
				profile_idc == 83 || profile_idc == 86 || profile_idc == 118 ||
				profile_idc == 128 ) {
			t.add(new DefaultMutableTreeNode(new KVP("chroma_format_idc",chroma_format_idc,getChroma_format_idcString(chroma_format_idc))));
			if( chroma_format_idc == 3 ){
				t.add(new DefaultMutableTreeNode(new KVP("separate_colour_plane_flag",separate_colour_plane_flag,null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("bit_depth_luma_minus8",bit_depth_luma_minus8,null)));
			t.add(new DefaultMutableTreeNode(new KVP("bit_depth_chroma_minus8",bit_depth_chroma_minus8,null)));
			t.add(new DefaultMutableTreeNode(new KVP("qpprime_y_zero_transform_bypass_flag",qpprime_y_zero_transform_bypass_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("seq_scaling_matrix_present_flag",seq_scaling_matrix_present_flag,null)));
			
			seq_scaling_matrix_present_flag =bitSource.u(1);
			if( seq_scaling_matrix_present_flag==1 ){
				for( int i = 0; i < 8; i++ ) { // TODO ? change to "for( i = 0; i < ( ( chroma_format_idc != 3 ) ? 8 : 12 ); i++ )"  as in Rec. ITU-T H.264 (03/2010) 7.3.2.1.1 Sequence parameter set data syntax
					t.add(new DefaultMutableTreeNode(new KVP("seq_scaling_list_present_flag["+i+"]",seq_scaling_list_present_flag[ i ],null)));
					if( seq_scaling_list_present_flag[ i ]!=0 ){
						if( i < 6 ){
							t.add(getScalingListJTree( delta_scale[i], i, 16,deltas_read[i]));
						}else{
							t.add(getScalingListJTree( delta_scale[i], i, 64,deltas_read[i]));
						}
					}
				}
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("log2_max_frame_num_minus4",log2_max_frame_num_minus4,"MaxFrameNum="+BitSource.powerOf2[log2_max_frame_num_minus4+4])));
		t.add(new DefaultMutableTreeNode(new KVP("pic_order_cnt_type",pic_order_cnt_type,null)));
		
		if(pic_order_cnt_type == 0){
			t.add(new DefaultMutableTreeNode(new KVP("log2_max_pic_order_cnt_lsb_minus4",log2_max_pic_order_cnt_lsb_minus4,null)));
		}

		t.add(new DefaultMutableTreeNode(new KVP("max_num_ref_frames",max_num_ref_frames,null)));
		t.add(new DefaultMutableTreeNode(new KVP("gaps_in_frame_num_value_allowed_flag",gaps_in_frame_num_value_allowed_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pic_width_in_mbs_minus1",pic_width_in_mbs_minus1,"PicWidthInSamples="+16*(pic_width_in_mbs_minus1+1))));
		t.add(new DefaultMutableTreeNode(new KVP("pic_height_in_map_units_minus1",pic_height_in_map_units_minus1,"PicHeightInSamples="+(( 2-frame_mbs_only_flag ) * (pic_height_in_map_units_minus1 + 1)*16))));
		t.add(new DefaultMutableTreeNode(new KVP("frame_mbs_only_flag",frame_mbs_only_flag,frame_mbs_only_flag==0?"coded pictures of the coded video sequence may either be coded fields or coded frames":"every coded picture of the coded video sequence is a coded frame containing only frame macroblocks")));
		if(frame_mbs_only_flag==0 ){
			t.add(new DefaultMutableTreeNode(new KVP("mb_adaptive_frame_field_flag",mb_adaptive_frame_field_flag,mb_adaptive_frame_field_flag==0?"no switching between frame and field macroblocks within a picture":"possible use of switching between frame and field macroblocks within frames" )));
		}

		t.add(new DefaultMutableTreeNode(new KVP("direct_8x8_inference_flag",direct_8x8_inference_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("frame_cropping_flag",frame_cropping_flag,null)));

		if( frame_cropping_flag!=0 ) {
			t.add(new DefaultMutableTreeNode(new KVP("frame_crop_left_offset",frame_crop_left_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("frame_crop_right_offset",frame_crop_right_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("frame_crop_top_offset",frame_crop_top_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("frame_crop_bottom_offset",frame_crop_bottom_offset,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("vui_parameters_present_flag",vui_parameters_present_flag,null)));
		if(vui_parameters_present_flag!=0){
			t.add(vui_parameters.getJTreeNode(modus));
		}

		return t;
	}

	
	public static String getProfileIdcString(final int profile_idc) {

		switch (profile_idc) {
		case 66: return "Baseline profile";
		case 77: return "Main profile";
		case 88: return "Extended profile";
		case 100: return "High profile";
		case 110: return "High 10 profile";
		case 122: return "High 4:2:2 profile";
		case 44: return "CAVLC 4:4:4 Intra profile";
		case 144: return "High 4:4:4 Predictive profile";
		
		// these are used in Rec. ITU-T H.264 (03/2010) – Prepublished version, but not defined in Annex A
		case 83: return "??"; 
		case 86: return "??";
		case 118: return "??";
		case 128: return "??";

		default:
			return "unknown";
		}
	}
	
	public static String getChroma_format_idcString(int chroma_format_idc){
		switch (chroma_format_idc) {
		case 0: return "monochrome";
		case 1: return "4:2:0";
		case 2: return "4:2:2";
		case 3: return "4:4:4";
			

		default:
			return "error";
		}
		
	}

}
